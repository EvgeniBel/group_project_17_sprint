package ru.practicum;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.StatRequestParamDto;
import ru.practicum.ewm.StatResponseDto;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class StatClientImpl implements StatClient {

    private final RestClient restClient;

    public StatClientImpl(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:9090")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public HitDto postHit(HitDto dto) {
        return restClient.post()
                .uri("/hit")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(HitDto.class);
    }

    @Override
    public List<StatResponseDto> getStats(StatRequestParamDto dto) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats")
                        .queryParam("start", dto.getStart())
                        .queryParam("end", dto.getEnd())
                        .queryParam("uris", dto.getUris())
                        .queryParam("unique", dto.getUnique())
                        .build())
                .accept(APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<StatResponseDto>>() {
                });
    }
}
