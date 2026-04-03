package ru.practicum.ewm.dto.event;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.ewm.constants.Constants;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class NewEventDtoJsonTest {
    private final JacksonTester<NewEventDto> json;

    LocationDto location = new LocationDto(33.56, 76.87);

    NewEventDto dto = NewEventDto.builder()
            .annotation("новое очень интересное событие")
            .category(3L)
            .description("супер новое очень интересное событие")
            .eventDate("2026-10-23 15:30:00")
            .location(location)
            .paid(true)
            .participantLimit(200)
            .requestModeration(true)
            .title("летающие слоны")
            .build();

    @Test
    void testNewEventDto() throws Exception {
        JsonContent<NewEventDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.annotation").isEqualTo("новое очень интересное событие");
        assertThat(result).extractingJsonPathNumberValue("$.category").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("супер новое очень интересное событие");
        assertThat(result).extractingJsonPathStringValue("$.eventDate")
                .isEqualTo(LocalDateTime.of(2026, 10, 23, 15, 30, 0).format(Constants.FORMATTER));
        assertThat(result).extractingJsonPathNumberValue("$.location.lat").isEqualTo(33.56);
        assertThat(result).extractingJsonPathNumberValue("$.location.lon").isEqualTo(76.87);
        assertThat(result).extractingJsonPathBooleanValue("$.paid").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.participantLimit").isEqualTo(200);
        assertThat(result).extractingJsonPathBooleanValue("$.requestModeration").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("летающие слоны");
    }

}