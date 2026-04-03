package ru.practicum.ewm.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCompilationDto {

    private List<Long> events;

    private Boolean pinned;

    @NotBlank
    @Size(max = 50, message = "Длина названия подборки должна быть не более 50 символов")
    private String title;

}
