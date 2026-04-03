package ru.practicum;

import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.StatRequestParamDto;
import ru.practicum.ewm.StatResponseDto;

import java.util.List;

public interface StatClient {

    /**
     * Метод для отправки HTTP-запроса в stat-service на добавление данных в статистику.
     *
     * @param dto Содержит данные для добавления.
     * @return Содержит добавленные данные с id записи в БД.
     */
    HitDto postHit(HitDto dto);

    /**
     * Метода для отправки HTTP-запроса в stat-service на получение данных статистики.
     *
     * @param dto Содержит параметры для фильтрации необходимых данных.
     * @return Содержит список данных статистики.
     */
    List<StatResponseDto> getStats(StatRequestParamDto dto);

    void saveHit(String uri, String app);
}