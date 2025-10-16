package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
@Validated
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@Valid @RequestBody NewFilmRequest request) {
        log.info("Получен запрос на создание фильма: {}", request.getName());
        return filmService.createFilm(request);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody UpdateFilmRequest request) {
        log.info("Получен запрос на обновление фильма с id: {}", request.getId());
        return filmService.updateFilm(request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public FilmDto getById(@PathVariable("id") @Positive Long filmId) {
        log.info("Получен запрос на получение фильма по id: {}", filmId);
        return filmService.getFilmById(filmId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> getAll() {
        log.info("Получен запрос на получение списка всех фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> getPopular(@RequestParam(defaultValue = "10") @Positive int count) {
        log.info("Получен запрос на получение списка популярных фильмов ТОП-{}", count);
        return filmService.getPopularFilms(count);
    }

    @PutMapping("/{film_id}/like/{id}")
    public boolean addLike(@PathVariable("film_id") @Positive Long filmId, @PathVariable("id") @Positive Long userId) {
        log.info("Получен запрос на добавление лайка фильму {} от пользователя {}", filmId, userId);
        return filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{film_id}/like/{id}")
    public boolean removeLike(@PathVariable("film_id") @Positive Long filmId, @PathVariable("id") @Positive Long userId) {
        log.info("Получен запрос на удаление лайка у фильма {} от пользователя {}", filmId, userId);
        return filmService.removeLike(filmId, userId);
    }

    @GetMapping("/common")
    public List<FilmDto> getCommonFilms(@RequestParam @Positive Long userId, @RequestParam @Positive Long friendId) {
        log.info("Получен запрос на поиск общих фильмов у пользователей с id {} и {}", userId, friendId);
        return filmService.getCommonFilm(userId, friendId);
    }
}