package ru.practicum.ewm.dto.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.ewm.annotation.NotBlankOrNull;

@Value
@Builder
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class UpdateEventAdminRequest {

    @NotBlankOrNull
    @Size(min = 20, max = 2000, message = "Длина аннотации должна быть не менее 20 и не более 2000 символов")
    String annotation;
    Long category;

    @NotBlankOrNull
    @Size(min = 20, max = 7000, message = "Длина описания события должна быть не менее 20 и не более 7000 символов")
    String description;
    String eventDate;
    @Valid
    LocationDto location;
    Boolean paid;
    Integer participantLimit;
    Boolean requestModeration;
    String stateAction;

    @NotBlankOrNull
    @Size(min = 3, max = 120, message = "Длина заголовка события должна быть не менее 3 и не более 120 символов")
    String title;
}