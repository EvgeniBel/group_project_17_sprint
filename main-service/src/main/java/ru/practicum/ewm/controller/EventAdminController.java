package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.AdminEventParam;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.service.EventService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventAdminController {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEventsByAdminParam(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Уровень Admin. Получение списка из {} событий по необходимым параметрам. " +
                "Пропускаем {} элементов. ", size, from);

        List<Long> validUsers = filterValidIds(users);
        List<Long> validCategories = filterValidIds(categories);

        AdminEventParam param = AdminEventParam.builder()
                .users(validUsers)
                .states(states)
                .categories(validCategories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        return eventService.getEventsByAdminParam(param);
    }

    /**
     * Фильтрует список ID, оставляя только положительные ( > 0 )
     */
    private List<Long> filterValidIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        List<Long> validIds = ids.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());
        return validIds.isEmpty() ? null : validIds;
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto patchEventByIdByAdmin(
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid UpdateEventAdminRequest dto
    ) {
        log.info("Уровень Admin. Обновление администратором данных события с ID: {}. ", eventId);
        return eventService.patchEventByIdByAdmin(eventId, dto);
    }
}