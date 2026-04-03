package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.request.ParticipationRequest;
import ru.practicum.ewm.model.request.RequestStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class RequestMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    Преобразование в сущность

    public ParticipationRequest toEntity(LocalDateTime nowData, Event event, User requester, RequestStatus status) {
        return ParticipationRequest.builder()
                .created(nowData)
                .event(event)
                .requester(requester)
                .status(status)
                .build();
    }

//    Преобразование в dto

    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest req) {
        return ParticipationRequestDto.builder()
                .id(req.getId())
                .created(req.getCreated().format(FORMATTER))
                .event(req.getEvent().getId())
                .requester(req.getRequester().getId())
                .status(req.getStatus().toString())
                .build();
    }

}