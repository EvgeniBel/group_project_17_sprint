package ru.practicum.ewm.dto.event;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class LocationDtoJsonTest {
    private final JacksonTester<LocationDto> json;

    LocationDto location = new LocationDto(33.56, 76.87);

    @Test
    void testLocationDto() throws Exception {
        JsonContent<LocationDto> result = json.write(location);

        assertThat(result).extractingJsonPathNumberValue("$.lat").isEqualTo(33.56);
        assertThat(result).extractingJsonPathNumberValue("$.lon").isEqualTo(76.87);
    }

}