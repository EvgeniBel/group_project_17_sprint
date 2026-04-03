package ru.practicum.ewm.dto.compilation;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationDto {

    private Long id;

    private List<Long> events;

    private Boolean pinned;
    @Size(max = 50, message = "Длина названия подборки должна быть не более 50 символов")
    private String title;
}