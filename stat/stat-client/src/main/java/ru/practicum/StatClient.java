package ru.practicum;

import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.StatRequestParamDto;
import ru.practicum.ewm.StatResponseDto;

import java.util.List;

public interface StatClient {

    //    Добавит запись в статистику
    HitDto postHit(HitDto dto);

    //    Получить статистику
    List<StatResponseDto> getStats(StatRequestParamDto dto);

}
