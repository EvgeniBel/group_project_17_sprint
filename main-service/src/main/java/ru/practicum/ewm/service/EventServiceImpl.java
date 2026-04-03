package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatClient;
import ru.practicum.ewm.StatRequestParamDto;
import ru.practicum.ewm.StatResponseDto;
import ru.practicum.ewm.constants.Constants;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exception.CreationRulesException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.LocationMapper;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.*;
import ru.practicum.ewm.model.request.ParticipationRequest;
import ru.practicum.ewm.model.request.RequestStatus;
import ru.practicum.ewm.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final StatClient statClient;

    @Transactional
    @Override
    public EventFullDto addEvent(Long userId, NewEventDto dto) {
        Category category = categoryRepository.findById(dto.getCategory()).orElseThrow(() -> new NotFoundException(
                "Добавление события. Категория с ID: " + dto.getCategory() + " не найдена."));

        User initiator = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                "Добавление события. Пользователь с ID: " + userId + " не найден."));

        Location location = locationRepository.save(LocationMapper.dtoToLocation(dto.getLocation()));

        LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), Constants.FORMATTER);

        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            log.error("Добавление события. Время начала события должно быть не ранее, чем через два часа от текущего момента.");
            throw new ValidationException("Время начала события должно быть не ранее, чем через два часа от текущего момента.");
        }

        if (dto.getPaid() == null) {
            dto.setPaid(false);
        }

        if (dto.getParticipantLimit() == null) {
            dto.setParticipantLimit(0);
        }

        if (dto.getRequestModeration() == null) {
            dto.setRequestModeration(true);
        }

        Event newEvent = EventMapper.dtoToEvent(
                dto,
                category,
                LocalDateTime.now(),
                initiator,
                location,
                null,
                EventState.PENDING
        );

        Event addedEvent = eventRepository.save(newEvent);
        return EventMapper.eventToFullDto(addedEvent, 0L, 0L);
    }

    @Override
    public List<EventShortDto> getEventsOfUser(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiator_IdOrderByEventDateAsc(userId, pageable);
        if (events == null || events.isEmpty()) {
            log.info("Получение списка событий пользователя. " +
                    "По заданным параметрам (userId: {}, from: {}, size {}) события не найдены.", userId, from, size);
            return new ArrayList<>();
        }

        List<StatResponseDto> stats = getViewsStats(events);

        List<EventShortDto> result = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            Long confirmedRequests = requestRepository.countByEvent_IdAndStatus(events.get(i).getId(),
                    RequestStatus.CONFIRMED);
            Long hits = getHitsForEvent(stats, i);
            EventShortDto eventShortDto = EventMapper.eventToShortDto(
                    events.get(i),
                    confirmedRequests,
                    hits);
            result.add(eventShortDto);
        }
        return result;
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Получения данных о событии. Событие с ID: " + eventId + " не найдено."));

        // Проверяем, что пользователь является инициатором события
        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Получение данных о событии. Пользователь с ID: {} не является инициатором события с ID: {}", userId, eventId);
            throw new NotFoundException("Пользователь с ID: " + userId + " не является инициатором события с ID: " + eventId);
        }

        List<StatResponseDto> stats = getViewsStats(List.of(event));
        Long hits = stats.isEmpty() ? 0L : stats.getFirst().getHits();

        return EventMapper.eventToFullDto(
                event,
                requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED),
                hits
        );
    }

    @Override
    public List<EventShortDto> getShortEventsInfoByIds(List<Long> eventIds) {
        List<Event> events = eventRepository.findAllByIdInOrderByIdAsc(eventIds);

        if (events == null || events.isEmpty()) {
            log.info("Получение событий по списку ID. По указанным в списке ID событий события не найдены.");
            return new ArrayList<>();
        }

        List<StatResponseDto> stats = getViewsStats(events);

        List<EventShortDto> result = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            Long confirmedRequests = requestRepository.countByEvent_IdAndStatus(
                    events.get(i).getId(),
                    RequestStatus.CONFIRMED
            );
            Long hits = getHitsForEvent(stats, i);
            EventShortDto eventShortDto = EventMapper.eventToShortDto(
                    events.get(i),
                    confirmedRequests,
                    hits
            );
            result.add(eventShortDto);
        }
        return result;
    }

    @Transactional
    @Override
    public EventFullDto patchEventById(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event oldEvent = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Обновление данных события. Событие с ID: " + eventId + " не найдено."));

        // Проверяем, что пользователь является инициатором
        if (!oldEvent.getInitiator().getId().equals(userId)) {
            log.error("Обновление данных события. Пользователь с ID: {} не является инициатором события с ID: {}", userId, eventId);
            throw new NotFoundException("Пользователь с ID: " + userId + " не является инициатором события с ID: " + eventId);
        }

        if (oldEvent.getState().equals(EventState.PUBLISHED)) {
            log.error("Обновление данных события. Изменить можно только отмененные события " +
                    "или события в состоянии ожидания модерации.");
            throw new CreationRulesException("Изменить можно только отмененные события " +
                    "или события в состоянии ожидания модерации.");
        }

        if (dto.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), Constants.FORMATTER);

            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                log.error("Обновление данных события. Время начала события должно быть не ранее, чем через два часа от текущего момента.");
                throw new ValidationException("Время начала события должно быть не ранее, чем через два часа от текущего момента.");
            }

            oldEvent.setEventDate(eventDate);
        }

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory()).orElseThrow(() -> new NotFoundException(
                    "Обновление данных события. Категория с ID: " + dto.getCategory() + " не найдена."));

            oldEvent.setCategory(category);
        }

        if (dto.getLocation() != null) {
            oldEvent.getLocation().setLat(dto.getLocation().getLat());
            oldEvent.getLocation().setLon(dto.getLocation().getLon());
        }

        if (dto.getAnnotation() != null) {
            oldEvent.setAnnotation(dto.getAnnotation());
        }

        if (dto.getDescription() != null) {
            oldEvent.setDescription(dto.getDescription());
        }

        if (dto.getPaid() != null) {
            oldEvent.setPaid(dto.getPaid());
        }

        if (dto.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(dto.getParticipantLimit());
        }

        if (dto.getRequestModeration() != null) {
            oldEvent.setRequestModeration(dto.getRequestModeration());
        }

        if (dto.getStateAction() != null) {
            if (dto.getStateAction().equals(UserStateAction.SEND_TO_REVIEW.toString())) {
                oldEvent.setState(EventState.PENDING);
            }
            if (dto.getStateAction().equals(UserStateAction.CANCEL_REVIEW.toString())) {
                oldEvent.setState(EventState.CANCELED);
            }
        }

        if (dto.getTitle() != null) {
            oldEvent.setTitle(dto.getTitle());
        }

        Event patchedEvent = eventRepository.save(oldEvent);
        List<StatResponseDto> stats = getViewsStats(List.of(patchedEvent));
        Long hits = stats.isEmpty() ? 0L : stats.getFirst().getHits();

        return EventMapper.eventToFullDto(
                patchedEvent,
                requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED),
                hits
        );
    }

    @Override
    public List<ParticipationRequestDto> getRequestsOfEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Получение запросов на участие в событии. Событие с ID: " + eventId + " не найдено."));

        // Проверяем, что пользователь является инициатором
        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Получение запросов на участие в событии. Пользователь с ID: {} не является инициатором события с ID: {}", userId, eventId);
            throw new NotFoundException("Пользователь с ID: " + userId + " не является инициатором события с ID: " + eventId);
        }

        List<ParticipationRequest> requests = requestRepository.findAllByEvent_Id(eventId);
        if (requests == null || requests.isEmpty()) {
            log.info("Получение запросов на участие в событии. По событию с ID: {} запросов не найдено.", eventId);
            return new ArrayList<>();
        }

        return requests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult patchRequestsStatusOfEvent(Long userId, Long eventId,
                                                                     EventRequestStatusUpdateRequest dto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Обновление данных события. Событие с ID: " + eventId + " не найдено."));

        // Проверяем, что пользователь является инициатором
        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Обновление статусов заявок. Пользователь с ID: {} не является инициатором события с ID: {}", userId, eventId);
            throw new NotFoundException("Пользователь с ID: " + userId + " не является инициатором события с ID: " + eventId);
        }

        // ИСПРАВЛЕНО: сначала проверяем, что заявки существуют и не CONFIRMED
        List<ParticipationRequest> allRequests = requestRepository.findAllById(dto.getRequestIds());

        if (allRequests.size() != dto.getRequestIds().size()) {
            log.error("Обновление статусов заявок. Некоторые заявки не найдены.");
            throw new NotFoundException("Некоторые заявки не найдены");
        }

        // Проверяем, что все заявки имеют статус PENDING
        for (ParticipationRequest request : allRequests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                log.error("Обновление статусов заявок. Заявка с ID: {} имеет статус {}, а не PENDING",
                        request.getId(), request.getStatus());
                // ИСПРАВЛЕНО: выбрасываем 409 Conflict
                throw new CreationRulesException("Статус можно изменить только у заявок, " +
                        "находящихся в состоянии ожидания: " + RequestStatus.PENDING +
                        ". Текущий статус заявки: " + request.getStatus());
            }
        }

        List<ParticipationRequest> requests = allRequests; // все заявки PENDING

        Long participantLimit = event.getParticipantLimit().longValue();
        Long approvedRequestsCount = requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (dto.getStatus() == RequestStatus.REJECTED) {
            // Отклоняем все заявки
            for (ParticipationRequest request : requests) {
                request.setStatus(RequestStatus.REJECTED);
            }
            requestRepository.saveAll(requests);

            List<ParticipationRequestDto> resultRejectedRequestsDto = requests.stream()
                    .map(RequestMapper::toParticipationRequestDto)
                    .toList();

            return new EventRequestStatusUpdateResult(List.of(), resultRejectedRequestsDto);
        }

        // Логика для CONFIRMED
        if (participantLimit.equals(approvedRequestsCount) && participantLimit > 0) {
            log.error("Обновление статусов заявок на участие в событии. " +
                    "Достигнут лимит одобренных заявок в событии с ID: {}.", eventId);
            throw new CreationRulesException("Достигнут лимит одобренных заявок в событии с ID: " + eventId + ".");
        }

        List<ParticipationRequest> approvedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (approvedRequestsCount < participantLimit || participantLimit == 0) {
                request.setStatus(RequestStatus.CONFIRMED);
                approvedRequests.add(request);
                approvedRequestsCount++;
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        }

        requestRepository.saveAll(approvedRequests);
        requestRepository.saveAll(rejectedRequests);

        List<ParticipationRequestDto> resultApprovedRequestsDto = approvedRequests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();
        List<ParticipationRequestDto> resultRejectedRequestsDto = rejectedRequests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();

        return new EventRequestStatusUpdateResult(resultApprovedRequestsDto, resultRejectedRequestsDto);
    }

    @Override
    public List<EventFullDto> getEventsByAdminParam(AdminEventParam param) {
        log.info("Уровень Admin. Получение списка событий по параметрам: users={}, states={}, categories={}, " +
                        "rangeStart={}, rangeEnd={}, from={}, size={}",
                param.getUsers(), param.getStates(), param.getCategories(),
                param.getRangeStart(), param.getRangeEnd(), param.getFrom(), param.getSize());

        // ИСПРАВЛЕНО: проверка на невалидные ID ДО запроса в БД
        boolean hasInvalidUsers = param.getUsers() != null && param.getUsers().isEmpty();
        boolean hasInvalidCategories = param.getCategories() != null && param.getCategories().isEmpty();

        if (hasInvalidUsers || hasInvalidCategories) {
            log.info("Уровень Admin. Переданы невалидные ID (0 или отрицательные). Возвращаем пустой список.");
            return new ArrayList<>();
        }

        // Если пользователи или категории не указаны, но есть другие фильтры
        Pageable pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());
        List<Event> events = eventRepository.findByAdminParam(param, pageable);

        if (events == null || events.isEmpty()) {
            log.info("Уровень Admin. Получение списка событий. По заданным параметрам события не найдены.");
            return new ArrayList<>();
        }

        List<StatResponseDto> stats = getViewsStats(events);

        List<EventFullDto> result = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            Long confirmedRequests = requestRepository.countByEvent_IdAndStatus(
                    events.get(i).getId(),
                    RequestStatus.CONFIRMED);
            Long hits = getHitsForEvent(stats, i);
            EventFullDto eventFullDto = EventMapper.eventToFullDto(
                    events.get(i),
                    confirmedRequests,
                    hits);
            result.add(eventFullDto);
        }
        return result;
    }

    @Transactional
    @Override
    public EventFullDto patchEventByIdByAdmin(Long eventId, UpdateEventAdminRequest dto) {

        Event oldEvent = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                "Уровень Admin. Обновление данных события. Событие с ID: " + eventId + " не найдено."));
        log.info("Публикация события. Текущий статус: {}, eventDate: {}",
                oldEvent.getState(), oldEvent.getEventDate());
        if (dto.getStateAction() != null) {
            if (dto.getStateAction().equals(AdminStateAction.PUBLISH_EVENT.toString())
                    && oldEvent.getState().equals(EventState.PENDING)) {
                oldEvent.setState(EventState.PUBLISHED);
                oldEvent.setPublishedOn(LocalDateTime.now());
            } else if (dto.getStateAction().equals(AdminStateAction.REJECT_EVENT.toString())
                    && !oldEvent.getState().equals(EventState.PUBLISHED)) {
                oldEvent.setState(EventState.CANCELED);
            } else {
                log.error("Уровень Admin. Обновление данных события. " +
                        "Опубликовать можно только событие, ожидающее публикации. " +
                        "Отклонить можно только событие, которое не опубликовано.");
                throw new CreationRulesException("Обновление данных события администратором. " +
                        "Опубликовать можно только событие, ожидающее публикации. " +
                        "Отклонить можно только событие, которое не опубликовано.");
            }
        }

        if (dto.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), Constants.FORMATTER);

            // ИСПРАВЛЕНО: проверка, что дата события не раньше чем через час от текущего момента
            if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                log.error("Уровень Admin. Обновление данных события. Время начала события должно быть не ранее, чем через час от текущего момента.");
                throw new ValidationException("Обновление данных события администратором. " +
                        "Время начала события должно быть не ранее, чем через час от текущего момента.");
            }

            oldEvent.setEventDate(eventDate);
        }

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory()).orElseThrow(() -> new NotFoundException(
                    "Обновление данных события администратором. " +
                            "Категория с ID: " + dto.getCategory() + " не найдена."));

            oldEvent.setCategory(category);
        }

        if (dto.getLocation() != null) {
            if (oldEvent.getLocation() != null) {
                oldEvent.getLocation().setLat(dto.getLocation().getLat());
                oldEvent.getLocation().setLon(dto.getLocation().getLon());
            } else {
                Location newLocation = locationRepository.save(LocationMapper.dtoToLocation(dto.getLocation()));
                oldEvent.setLocation(newLocation);
            }
        }

        if (dto.getAnnotation() != null) {
            oldEvent.setAnnotation(dto.getAnnotation());
        }

        if (dto.getDescription() != null) {
            oldEvent.setDescription(dto.getDescription());
        }

        if (dto.getPaid() != null) {
            oldEvent.setPaid(dto.getPaid());
        }

        if (dto.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(dto.getParticipantLimit());
        }

        if (dto.getRequestModeration() != null) {
            oldEvent.setRequestModeration(dto.getRequestModeration());
        }

        if (dto.getTitle() != null) {
            oldEvent.setTitle(dto.getTitle());
        }

        Event patchedEvent = eventRepository.save(oldEvent);
        log.info("После сохранения: статус = {}, publishedOn = {}",
                patchedEvent.getState(), patchedEvent.getPublishedOn());
        List<StatResponseDto> stats = getViewsStats(List.of(patchedEvent));
        Long hits = stats.isEmpty() ? 0L : stats.getFirst().getHits();

        return EventMapper.eventToFullDto(
                patchedEvent,
                requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED),
                hits
        );
    }

    @Override
    public List<EventShortDto> getPublicEvents(PublicEventParam param) {
        log.info("Публичный поиск событий: {}", param);

        // Валидация диапазона дат
        LocalDateTime rangeStart = null;
        LocalDateTime rangeEnd = null;

        if (param.getRangeStart() != null && !param.getRangeStart().isBlank()) {
            rangeStart = LocalDateTime.parse(param.getRangeStart(), Constants.FORMATTER);
        }
        if (param.getRangeEnd() != null && !param.getRangeEnd().isBlank()) {
            rangeEnd = LocalDateTime.parse(param.getRangeEnd(), Constants.FORMATTER);
        }

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала диапазона не может быть позже даты окончания");
        }

        // Если диапазон не указан, ищем события после текущего момента
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        int page = param.getFrom() / param.getSize();
        Pageable pageable = PageRequest.of(page, param.getSize());

        List<Event> events;

        // Обработка параметров: пустые списки превращаем в null
        List<Long> categories = (param.getCategories() != null && param.getCategories().isEmpty())
                ? null : param.getCategories();
        String text = (param.getText() != null && param.getText().isBlank())
                ? null : param.getText();

        if (param.getOnlyAvailable() != null && param.getOnlyAvailable()) {
            events = eventRepository.findPublicEventsWithAvailableCheck(
                    EventState.PUBLISHED,
                    categories,
                    param.getPaid(),
                    text,
                    rangeStart,
                    rangeEnd,
                    true,
                    pageable
            );
        } else {
            events = eventRepository.findPublicEvents(
                    EventState.PUBLISHED,
                    categories,
                    param.getPaid(),
                    text,
                    rangeStart,
                    rangeEnd,
                    pageable
            );
        }

        if (events == null || events.isEmpty()) {
            log.info("Публичный эндпоинт. По заданным параметрам события не найдены.");
            return new ArrayList<>();
        }

        // Сохраняем информацию о просмотре для каждого события
        for (Event event : events) {
            saveEventViewToStats(event.getId());
        }

        List<StatResponseDto> stats = getViewsStats(events);

        List<EventShortDto> result = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            Long confirmedRequests = requestRepository.countByEvent_IdAndStatus(
                    events.get(i).getId(),
                    RequestStatus.CONFIRMED);
            Long hits = getHitsForEvent(stats, i);
            result.add(EventMapper.eventToShortDto(events.get(i), confirmedRequests, hits));
        }

        // Сортировка
        if (param.getSort() != null) {
            if (param.getSort().equals("VIEWS")) {
                result.sort((e1, e2) -> Long.compare(e2.getViews(), e1.getViews()));
            } else if (param.getSort().equals("EVENT_DATE")) {
                result.sort((e1, e2) -> {
                    LocalDateTime date1 = LocalDateTime.parse(e1.getEventDate(), Constants.FORMATTER);
                    LocalDateTime date2 = LocalDateTime.parse(e2.getEventDate(), Constants.FORMATTER);
                    return date1.compareTo(date2);
                });
            }
        }

        return result;
    }

    @Override
    public EventFullDto getPublicEventById(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException(
                "Публичный эндпоинт. Событие с ID: " + id + " не найдено."));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.error("Публичный эндпоинт. Событие с ID: {} не опубликовано.", id);
            throw new NotFoundException("Событие с ID: " + id + " не опубликовано или не найдено.");
        }

        // Сохраняем информацию о просмотре
        saveEventViewToStats(id);

        List<StatResponseDto> stats = getViewsStats(List.of(event));
        Long hits = stats.isEmpty() ? 0L : stats.getFirst().getHits();

        return EventMapper.eventToFullDto(
                event,
                requestRepository.countByEvent_IdAndStatus(id, RequestStatus.CONFIRMED),
                hits
        );
    }

    // Вспомогательные методы

    private void saveEventViewToStats(Long eventId) {
        try {
            statClient.saveHit("/events/" + eventId, "ewm-main-service");
        } catch (Exception e) {
            log.warn("Не удалось сохранить статистику просмотра для события {}", eventId, e);
        }
    }

    private Long getHitsForEvent(List<StatResponseDto> stats, int index) {
        if (stats == null || stats.isEmpty() || index >= stats.size()) {
            return 0L;
        }
        StatResponseDto stat = stats.get(index);
        return stat != null ? stat.getHits() : 0L;
    }

    private List<StatResponseDto> getViewsStats(List<Event> events) {
        if (events == null || events.isEmpty()) {
            log.debug("Получение статистики: список событий пуст");
            return new ArrayList<>();
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime start;
        LocalDateTime end;

        if (events.getFirst().getEventDate() != null) {
            start = events.getFirst().getEventDate().minusHours(36L);
        } else {
            start = LocalDateTime.now().minusDays(30);
        }

        if (events.getLast().getEventDate() != null) {
            end = events.getLast().getEventDate().plusHours(36L);
        } else {
            end = LocalDateTime.now().plusDays(1);
        }

        StatRequestParamDto statRequestParamDto = new StatRequestParamDto(
                start.format(Constants.FORMATTER),
                end.format(Constants.FORMATTER),
                uris,
                true
        );

        try {
            return statClient.getStats(statRequestParamDto);
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}