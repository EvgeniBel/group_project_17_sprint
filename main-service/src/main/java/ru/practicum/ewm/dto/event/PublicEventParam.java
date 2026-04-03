package ru.practicum.ewm.dto.event;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PublicEventParam {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private String rangeStart;
    private String rangeEnd;
    private Boolean onlyAvailable;
    private String sort;
    private Integer from;
    private Integer size;
}