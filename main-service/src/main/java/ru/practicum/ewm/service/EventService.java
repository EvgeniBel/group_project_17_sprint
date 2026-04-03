package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;

import java.util.List;

public interface EventService {

    EventFullDto addEvent(Long userId, NewEventDto dto);

    List<EventShortDto> getEventsOfUser(Long userId, Integer from, Integer size);

    EventFullDto getEventById(Long userId, Long eventId);

    List<EventShortDto> getShortEventsInfoByIds(List<Long> eventIds);

    EventFullDto patchEventById(Long userId, Long eventId, UpdateEventUserRequest dto);

    List<ParticipationRequestDto> getRequestsOfEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult patchRequestsStatusOfEvent(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest dto);

    List<EventFullDto> getEventsByAdminParam(AdminEventParam param);

    EventFullDto patchEventByIdByAdmin(Long eventId, UpdateEventAdminRequest dto);

    List<EventShortDto> getPublicEvents(PublicEventParam param);

    EventFullDto getPublicEventById(Long id);
}