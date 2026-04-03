package ru.practicum.ewm.dto.compilation;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetManyCompilationDto {
    Boolean pinned;
    Integer from;
    Integer size;
}
