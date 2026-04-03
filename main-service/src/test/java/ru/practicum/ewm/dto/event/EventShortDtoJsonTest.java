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
class EventShortDtoJsonTest {
    private final JacksonTester<EventShortDto> json;

    CategoryDto category = new CategoryDto(3L, "Цирк");
    UserShortDto initiator = new UserShortDto(1L, "Коля");

    EventShortDto dto = EventShortDto.builder()
            .annotation("рок-концерт вселенского масштаба")
            .category(category)
            .confirmedRequests(10654L)
            .eventDate("2026-11-12 10:00:00")
            .id(7L)
            .initiator(initiator)
            .paid(true)
            .title("поющие электронные гитары")
            .views(64516L)
            .build();

    @Test
    void testEventShortDto() throws Exception {
        JsonContent<EventShortDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.annotation").isEqualTo("рок-концерт вселенского масштаба");
        assertThat(result).extractingJsonPathNumberValue("$.category.id").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.category.name").isEqualTo("Цирк");
        assertThat(result).extractingJsonPathNumberValue("$.confirmedRequests").isEqualTo(10654);
        assertThat(result).extractingJsonPathStringValue("$.eventDate")
                .isEqualTo(LocalDateTime.of(2026, 11, 12, 10, 0, 0).format(Constants.FORMATTER));
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(7);
        assertThat(result).extractingJsonPathNumberValue("$.initiator.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.initiator.name").isEqualTo("Коля");
        assertThat(result).extractingJsonPathBooleanValue("$.paid").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.title").isEqualTo("поющие электронные гитары");
        assertThat(result).extractingJsonPathNumberValue("$.views").isEqualTo(64516);
    }

}