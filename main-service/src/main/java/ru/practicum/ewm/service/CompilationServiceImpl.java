package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.CreateCompilationDto;
import ru.practicum.ewm.dto.compilation.GetManyCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    @Transactional
    @Override
    public CompilationDto createCompilation(CreateCompilationDto dto) {
        log.info("Создание подборки: title={}, events={}, pinned={}",
                dto.getTitle(), dto.getEvents(), dto.getPinned());

        try {
            List<Event> events = new ArrayList<>();
            List<Long> eventIds = dto.getEvents();

            if (eventIds != null && !eventIds.isEmpty()) {
                events = eventRepository.findAllById(eventIds);
                log.debug("Найдено событий: {} из {}", events.size(), eventIds.size());
            }

            Compilation compilation = CompilationMapper.toEntity(dto, events);
            Compilation saved = compilationRepository.save(compilation);
            log.info("Подборка создана с id: {}", saved.getId());

            return CompilationMapper.toCompilationDto(saved,
                    eventService.getShortEventsInfoByIds(eventIds != null ? eventIds : List.of()));
        } catch (Exception e) {
            log.error("Ошибка при создании подборки: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(UpdateCompilationDto dto) {
        log.info("Обновление подборки с id: {}", dto.getId());
        log.info("Получены события для добавления: {}", dto.getEvents());

        Compilation compilation = compilationRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + dto.getId() + " не найдена"));

        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }

        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            log.info("Поиск событий по ID: {}", dto.getEvents());
            List<Event> events = eventRepository.findAllById(dto.getEvents());
            log.info("Найдено событий: {} из {}", events.size(), dto.getEvents().size());

            if (events.size() != dto.getEvents().size()) {
                log.error("События не найдены: {}", dto.getEvents());
                throw new NotFoundException("Некоторые события не найдены");
            }

            // Очищаем старые связи и добавляем новые
            compilation.getEvents().clear();
            compilation.getEvents().addAll(events);
            log.info("События добавлены в подборку");
        }

        Compilation updated = compilationRepository.save(compilation);
        log.info("Подборка сохранена, количество событий: {}", updated.getEvents().size());

        List<Long> eventIds = updated.getEvents().stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        return CompilationMapper.toCompilationDto(updated,
                eventService.getShortEventsInfoByIds(eventIds));
    }

    @Transactional
    @Override
    public void removeCompilation(Long compId) {
        log.info("Удаление подборки с id: {}", compId);

        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка с id=" + compId + " не найдена");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getCompilations(GetManyCompilationDto dto) {
        log.info("Получение подборок: pinned={}, from={}, size={}",
                dto.getPinned(), dto.getFrom(), dto.getSize());

        // ИСПРАВЛЕНО: правильная пагинация
        int page = dto.getFrom() / dto.getSize();
        Pageable pageable = PageRequest.of(page, dto.getSize());

        List<Compilation> compilations;
        if (dto.getPinned() != null) {
            compilations = compilationRepository.findAllByPinned(dto.getPinned(), pageable)
                    .getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        if (compilations.isEmpty()) {
            return new ArrayList<>();
        }

        // Собираем все ID событий из всех подборок
        List<Long> allEventIds = compilations.stream()
                .flatMap(c -> c.getEvents().stream())
                .map(Event::getId)
                .distinct()
                .collect(Collectors.toList());

        // Получаем все события одной пачкой
        List<EventShortDto> allEvents = eventService.getShortEventsInfoByIds(allEventIds);
        Map<Long, EventShortDto> eventMap = allEvents.stream()
                .collect(Collectors.toMap(EventShortDto::getId, e -> e));

        // Формируем результат
        return compilations.stream()
                .map(comp -> CompilationMapper.toCompilationDto(
                        comp,
                        comp.getEvents().stream()
                                .map(e -> eventMap.get(e.getId()))
                                .filter(e -> e != null)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Получение подборки по id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        List<Long> eventIds = compilation.getEvents().stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        return CompilationMapper.toCompilationDto(compilation,
                eventService.getShortEventsInfoByIds(eventIds));
    }
}