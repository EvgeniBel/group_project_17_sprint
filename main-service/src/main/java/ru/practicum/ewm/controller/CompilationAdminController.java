package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.CreateCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationDto;
import ru.practicum.ewm.service.CompilationService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class CompilationAdminController {

    private final CompilationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(
            @Valid @RequestBody CreateCompilationDto dto) {
        if (dto.getPinned() == null)
            dto.setPinned(false);
        return service.createCompilation(dto);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(
            @PositiveOrZero @PathVariable Long compId,
            @Valid @RequestBody UpdateCompilationDto dto) {  // ← ИСПРАВЛЕНО: используем UpdateCompilationDto
        log.info("PATCH /admin/compilations/{} - обновление подборки: events={}, pinned={}, title={}",
                compId, dto.getEvents(), dto.getPinned(), dto.getTitle());

        // Собираем DTO для сервиса
        UpdateCompilationDto serviceDto = UpdateCompilationDto.builder()
                .id(compId)
                .events(dto.getEvents())
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .build();

        return service.updateCompilation(serviceDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCompilation(
            @PositiveOrZero @PathVariable Long compId) {
        service.removeCompilation(compId);
    }

}