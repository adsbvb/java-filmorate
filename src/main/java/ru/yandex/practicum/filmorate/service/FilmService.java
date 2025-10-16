package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmJdbcStorage;
import ru.yandex.practicum.filmorate.dal.GenreJdbcStorage;
import ru.yandex.practicum.filmorate.dal.MpaJdbcStorage;
import ru.yandex.practicum.filmorate.dal.UserJdbcStorage;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmJdbcStorage filmJdbcStorage;
    private final UserJdbcStorage userJdbcStorage;
    private final GenreJdbcStorage genreJdbcStorage;
    private final MpaJdbcStorage mpaJdbcStorage;

    @Autowired
    public FilmService(FilmJdbcStorage filmJdbcStorage, UserJdbcStorage userJdbcStorage, GenreJdbcStorage genreJdbcStorage, MpaJdbcStorage mpaJdbcStorage) {
        this.filmJdbcStorage = filmJdbcStorage;
        this.userJdbcStorage = userJdbcStorage;
        this.genreJdbcStorage = genreJdbcStorage;
        this.mpaJdbcStorage = mpaJdbcStorage;
    }

    public FilmDto createFilm(NewFilmRequest request) {
        log.info("Создание нового фильма с данными: {}", request);
        Film film = FilmMapper.mapToFilm(request);
        film = filmJdbcStorage.save(film);
        genreJdbcStorage.saveFilmGenres(film);
        log.info("Фильм успешно создан с id: {}", film.getId());
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        log.info("Обновление фильма с id: {}", request.getId());
        Film updatedFilm = filmJdbcStorage.findById(request.getId())
                .map(film -> {
                    log.debug("Обновляем поля фильма: {}", film);
                    return FilmMapper.updateFilmFields(film, request);
                })
                .orElseThrow(() -> {
                    log.warn("Фильм с id {} не найден для обновления", request.getId());
                    return new NotFoundException("Фильм для обновления не найден с id: " + request.getId());
                });
        updatedFilm = filmJdbcStorage.update(updatedFilm);
        genreJdbcStorage.updateFilmGenres(updatedFilm);
        log.info("Фильм с id {} обновлен успешно", updatedFilm.getId());
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public FilmDto getFilmById(Long filmId) {
        log.info("Получение фильма по id: {}", filmId);
        Optional<Film> filmOpt = filmJdbcStorage.findById(filmId);
        filmOpt.ifPresent(genreJdbcStorage::loadFilmGenres);
        filmOpt.ifPresent(mpaJdbcStorage::loadFilmMpa);
        return filmOpt.map(film -> {
                    log.info("Фильм найден: {}", film);
                    return FilmMapper.mapToFilmDto(film);
                })
                .orElseThrow(() -> {
                    log.warn("Фильм с id {} не найден", filmId);
                    return new NotFoundException("Фильм не найден с id: " + filmId);
                });
    }

    public List<FilmDto> getAllFilms() {
        log.info("Получение списка всех фильмов");
        List<Film> films = filmJdbcStorage.findAll();
        log.info("Найдено {} фильмов", films.size());
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public boolean addLike(Long filmId, Long userId) {
        log.info("Добавление лайка: пользователь {} ставит лайк фильму {}", userId, filmId);
        filmJdbcStorage.findById(filmId).orElseThrow(() -> {
            log.warn("Фильм с id {} не найден при добавлении лайка", filmId);
            return new NotFoundException("Фильм не найден при добавлении лайка с id: " + filmId);
        });
        userJdbcStorage.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь с id {} не найден при добавлении лайка", userId);
            return new NotFoundException("Пользователь не найден при добавлении лайка с id: " + userId);
        });
        return filmJdbcStorage.addLike(filmId, userId);
    }

    public boolean removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка: пользователь {} удаляет лайк у фильма {}", userId, filmId);
        filmJdbcStorage.findById(filmId).orElseThrow(() -> {
            log.warn("Фильм с id {} не найден при удалении лайка", filmId);
            return new NotFoundException("Фильм не найден при удалении лайка с id: " + filmId);
        });
        userJdbcStorage.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь с id {} не найден при удалении лайка", userId);
            return new NotFoundException("Пользователь не найден при удалении лайка с id: " + userId);
        });
        return filmJdbcStorage.removeLike(filmId, userId);
    }

    public List<FilmDto> getPopularFilms(int count) {
        log.info("Получение {} популярных фильмов", count);
        List<Film> films = filmJdbcStorage.getPopular(count);
        log.info("Найдено {} популярных фильмов", films.size());
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getCommonFilm(Long userId, Long friendId) {
        log.info("Получение общих фильмов у пользователей {} и {}", userId, friendId);
        userJdbcStorage.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь с id {} не найден при добавлении лайка", userId);
            return new NotFoundException("Пользователь с id " + userId + "не найден");
        });
        userJdbcStorage.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь с id {} не найден при добавлении лайка", friendId);
            return new NotFoundException("Пользователь с id " + friendId + "не найден");
        });

        List<Film> commonFilms = filmJdbcStorage.getCommonFilm(userId, friendId);
        log.info("Найдено {} общих фильмов", commonFilms.size());

        return commonFilms.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}