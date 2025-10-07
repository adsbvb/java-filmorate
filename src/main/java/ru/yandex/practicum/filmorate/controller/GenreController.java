package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
@Validated
public class GenreController {
    private final GenreService genreService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Genre getById(@PathVariable("id") @Positive Integer genreId) {
        log.info("Получен запрос на получения жанра с id: {}", genreId);
        return genreService.getGenreById(genreId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Genre> getAll() {
        log.info("Получен запрос на получения списка всех жанров");
        return genreService.getAllGenres();
    }
}