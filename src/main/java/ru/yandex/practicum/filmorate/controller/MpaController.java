package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
@Validated
public class MpaController {
    private final MpaService mpaService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mpa getById(@PathVariable("id") @Positive Integer mpaId) {
        log.info("Получен запрос на получения MPA-рейтинга с id: {}", mpaId);
        return mpaService.getMpaById(mpaId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Mpa> getAll() {
        log.info("Получен запрос на получение списка всех MPA-рейтингов");
        return mpaService.getMpaAll();
    }
}
