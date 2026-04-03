package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor
public class AdminEventParam {
    List<@Positive Long> users;
    List<String> states;
    List<@Positive Long> categories;
    String rangeStart;

    String rangeEnd;

    @PositiveOrZero
    Integer from;

    @Positive
    Integer size;
}