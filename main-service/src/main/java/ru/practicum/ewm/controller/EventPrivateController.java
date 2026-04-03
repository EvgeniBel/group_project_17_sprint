package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventPrivateController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(
            @PathVariable
            @Positive Long userId,
            @RequestBody
            @NotNull(message = "Добавляемое событие не может быть null")
            @Valid NewEventDto dto
    ) {
        if (dto.getPaid() == null)
            dto.setPaid(false);
        if (dto.getParticipantLimit() == null)
            dto.setParticipantLimit(0);
        if (dto.getRequestModeration() == null)
            dto.setRequestModeration(true);
        log.info("Создание нового события {} пользователем с ID: {}", dto, userId);
        return eventService.addEvent(userId, dto);
    }

    @GetMapping
    public List<EventShortDto> getEventsOfUser(
            @PathVariable
            @Positive Long userId,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10")
            @Positive Integer size
    ) {
        log.info("Получение списка из {} событий пользователя с ID: {}. Пропускаем {} элементов. ", size, userId, from);
        return eventService.getEventsOfUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(
            @PathVariable
            @Positive Long userId,
            @PathVariable
            @Positive Long eventId
    ) {
        log.info("Получение пользователем с ID: {} созданного им события с ID: {}. ", userId, eventId);
        return eventService.getEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto patchEventById(
            @PathVariable
            @Positive Long userId,
            @PathVariable
            @Positive Long eventId,
            @RequestBody
            @NotNull(message = "Данные для обновления события не могут быть null")
            @Valid UpdateEventUserRequest dto
    ) {
        log.info("Обновление пользователем с ID: {} созданного им события с ID: {}. ", userId, eventId);
        return eventService.patchEventById(userId, eventId, dto);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsOfEvent(
            @PathVariable
            @Positive Long userId,
            @PathVariable
            @Positive Long eventId
    ) {
        log.info("Получение пользователем с ID: {} запросов на участие в созданном им событии с ID: {}. ",
                userId, eventId);
        return eventService.getRequestsOfEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult patchRequestsStatusOfEvent(
            @PathVariable
            @Positive Long userId,
            @PathVariable
            @Positive Long eventId,
            @RequestBody
            @NotNull(message = "Данные для обновления события не могут быть null")
            @Valid EventRequestStatusUpdateRequest dto
    ) {
        log.info("Обновление пользователем с ID: {} статусов запросов на участие в созданном им событии с ID: {}. ",
                userId, eventId);
        return eventService.patchRequestsStatusOfEvent(userId, eventId, dto);
    }

}