package ru.practicum.ewm.controller;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.GetManyCompilationDto;
import ru.practicum.ewm.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class CompilationController {

    private final CompilationService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> getCompilations(
            @RequestParam(defaultValue = "true") Boolean pinned,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @PositiveOrZero @RequestParam(defaultValue = "10") Integer size
    ) {
        return service.getCompilations(GetManyCompilationDto.builder()
                .pinned(pinned)
                .from(from)
                .size(size)
                .build()
        );
    }

    @GetMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto getCompilationById(
            @PathVariable Long compId
    ) {
        return service.getCompilationById(compId);
    }


}