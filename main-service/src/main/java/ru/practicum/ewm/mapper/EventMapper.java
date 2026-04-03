package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.constants.Constants;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.event.Location;

import java.time.LocalDateTime;

@UtilityClass
public class EventMapper {

    public Event dtoToEvent(
            NewEventDto eventDto,
            Category category,
            LocalDateTime createdOn,
            User initiator,
            Location location,
            LocalDateTime publishedOn,
            EventState state
    ) {
        return Event.builder()
                .annotation(eventDto.getAnnotation())
                .category(category)
                .createdOn(createdOn)
                .description(eventDto.getDescription())
                .eventDate(LocalDateTime.parse(eventDto.getEventDate(), Constants.FORMATTER))
                .initiator(initiator)
                .location(location)
                .paid(eventDto.getPaid())
                .participantLimit(eventDto.getParticipantLimit())
                .publishedOn(publishedOn)
                .requestModeration(eventDto.getRequestModeration())
                .state(state)
                .title(eventDto.getTitle())
                .build();
    }

    public EventFullDto eventToFullDto(
            Event event,
            Long confirmedRequests,
            Long views
    ) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn().format(Constants.FORMATTER))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(Constants.FORMATTER))
                .id(event.getId())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(LocationMapper.locationToDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() != null ? event.getPublishedOn().format(Constants.FORMATTER) : null)
                .requestModeration(event.getRequestModeration())
                .state(event.getState().toString())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public EventShortDto eventToShortDto(
            Event event,
            Long confirmedRequests,
            Long views
    ) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate().format(Constants.FORMATTER))
                .id(event.getId())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

}