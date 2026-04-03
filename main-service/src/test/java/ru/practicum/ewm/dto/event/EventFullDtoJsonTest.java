package ru.practicum.ewm.dto.event;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.ewm.constants.Constants;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class EventFullDtoJsonTest {
    private final JacksonTester<EventFullDto> json;

    LocationDto location = new LocationDto(33.56, 76.87);
    CategoryDto category = new CategoryDto(3L, "Цирк");
    UserShortDto initiator = new UserShortDto(1L, "Коля");

    EventFullDto dto = EventFullDto.builder()
            .annotation("новое очень интересное событие")
            .category(category)
            .confirmedRequests(153L)
            .createdOn("2026-05-15 10:25:46")
            .description("супер новое очень интересное событие")
            .eventDate("2026-10-23 15:30:00")
            .id(7L)
            .initiator(initiator)
            .location(location)
            .paid(true)
            .participantLimit(200)
            .publishedOn("2026-05-16 18:46:13")
            .requestModeration(true)
            .state("PUBLISHED")
            .title("летающие слоны")
            .views(567L)
            .build();

    @Test
    void testEventFullDto() throws Exception {
        JsonContent<EventFullDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.annotation").isEqualTo("новое очень интересное событие");
        assertThat(result).extractingJsonPathNumberValue("$.category.id").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.category.name").isEqualTo("Цирк");
        assertThat(result).extractingJsonPathNumberValue("$.confirmedRequests").isEqualTo(153);
        assertThat(result).extractingJsonPathStringValue("$.createdOn")
                .isEqualTo(LocalDateTime.of(2026, 5, 15, 10, 25, 46).format(Constants.FORMATTER));
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("супер новое очень интересное событие");
        assertThat(result).extractingJsonPathStringValue("$.eventDate")
                .isEqualTo(LocalDateTime.of(2026, 10, 23, 15, 30, 0).format(Constants.FORMATTER));
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(7);
        assertThat(result).extractingJsonPathNumberValue("$.initiator.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.initiator.name").isEqualTo("Коля");
        assertThat(result).extractingJsonPathNumberValue("$.location.lat").isEqualTo(33.56);
        assertThat(result).extractingJsonPathNumberValue("$.location.lon").isEqualTo(76.87);
        assertThat(result).extractingJsonPathBooleanValue("$.paid").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.participantLimit").isEqualTo(200);
        assertThat(result).extractingJsonPathStringValue("$.publishedOn")
                .isEqualTo(LocalDateTime.of(2026, 5, 16, 18, 46, 13).format(Constants.FORMATTER));
        assertThat(result).extractingJsonPathBooleanValue("$.requestModeration").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.state").isEqualTo("PUBLISHED");
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("летающие слоны");
        assertThat(result).extractingJsonPathNumberValue("$.views").isEqualTo(567);
    }

}