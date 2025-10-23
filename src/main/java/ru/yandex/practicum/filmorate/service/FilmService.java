package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmJdbcStorage filmJdbcStorage;
    private final UserJdbcStorage userJdbcStorage;
    private final GenreJdbcStorage genreJdbcStorage;
    private final MpaJdbcStorage mpaJdbcStorage;
    private final DirectorJdbcStorage directorJdbcStorage;
    private final FilmDirectorRepository filmDirectorRepository;
    private final EventRepository eventRepository;

    @Autowired
    public FilmService(FilmJdbcStorage filmJdbcStorage, UserJdbcStorage userJdbcStorage, GenreJdbcStorage genreJdbcStorage, MpaJdbcStorage mpaJdbcStorage,
                       DirectorJdbcStorage directorJdbcStorage,
                       FilmDirectorRepository filmDirectorRepository, EventRepository eventRepository) {
        this.filmJdbcStorage = filmJdbcStorage;
        this.userJdbcStorage = userJdbcStorage;
        this.genreJdbcStorage = genreJdbcStorage;
        this.mpaJdbcStorage = mpaJdbcStorage;
        this.directorJdbcStorage = directorJdbcStorage;
        this.filmDirectorRepository = filmDirectorRepository;
        this.eventRepository = eventRepository;
    }

    public FilmDto createFilm(NewFilmRequest request) {
        log.info("Создание нового фильма с данными: {}", request);

        Film film = filmJdbcStorage.save(FilmMapper.mapToFilm(request));
        genreJdbcStorage.saveFilmGenres(film);
        saveFilmDirectorsIfPresent(request, film.getId());
        loadAllFilmRelations(film);

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
        if (request.hasDirectors()) {
            Set<Long> directorsIds = request.getDirectors().stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());
            filmDirectorRepository.updateFilmDirectors(updatedFilm.getId(), directorsIds);
        } else {
            filmDirectorRepository.updateFilmDirectors(updatedFilm.getId(), Collections.emptySet());
        }

        loadAllFilmRelations(updatedFilm);
        log.info("Фильм с id {} обновлен успешно", updatedFilm.getId());
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public FilmDto getFilmById(Long filmId) {
        log.info("Получение фильма по id: {}", filmId);
        Optional<Film> filmOpt = filmJdbcStorage.findById(filmId);
        filmOpt.ifPresent(genreJdbcStorage::loadFilmGenres);
        filmOpt.ifPresent(mpaJdbcStorage::loadFilmMpa);
        filmOpt.ifPresent(filmDirectorRepository::loadFilmDirectors);
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
        films = genreJdbcStorage.getGenresByFilms(films);
        films = mpaJdbcStorage.getMpaByFilms(films);
        films = filmDirectorRepository.getDirectorByFilms(films);
        log.info("Найдено {} фильмов", films.size());
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsByDirectorId(Long directorId, String sortBy) {
        log.info("Получение фильмов режиссера {} с сортировкой по: {}", directorId, sortBy);
        directorJdbcStorage.findById(directorId).orElseThrow(() -> {
            log.warn("Режиссер с id {} не найден", directorId);
            return new NotFoundException("Режиссер не найден с id: " + directorId);
        });

        List<Film> films = filmDirectorRepository.findFilmsByDirectorId(directorId, sortBy);
        log.info("Найдено {} фильмов режиссера {}", films.size(), directorId);
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
        eventRepository.addEvent(userId, "LIKE", "ADD", filmId);
        return filmJdbcStorage.addLike(filmId, userId);
    }

    public boolean removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка: пользователь {} удаляет лайк у фильма {}", userId, filmId);
        if (userId <= 0) {
            throw new NotFoundException("Пользователь не найден с id: " + userId);
        }
        filmJdbcStorage.findById(filmId).orElseThrow(() -> {
            log.warn("Фильм с id {} не найден при удалении лайка", filmId);
            return new NotFoundException("Фильм не найден при удалении лайка с id: " + filmId);
        });
        userJdbcStorage.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь с id {} не найден при удалении лайка", userId);
            return new NotFoundException("Пользователь не найден при удалении лайка с id: " + userId);
        });
        eventRepository.addEvent(userId, "LIKE", "REMOVE", filmId);
        return filmJdbcStorage.removeLike(filmId, userId);
    }

    public List<FilmDto> getPopularFilms(int count, Integer genreId, Integer releaseYear) {
        log.info("Получение {} популярных фильмов", count);
        List<Film> films = filmJdbcStorage.getPopular(count, genreId, releaseYear);
        films = genreJdbcStorage.getGenresByFilms(films);
        films = mpaJdbcStorage.getMpaByFilms(films);
        films = filmDirectorRepository.getDirectorByFilms(films);
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
        userJdbcStorage.findById(friendId).orElseThrow(() -> {
            log.warn("Пользователь с id {} не найден при добавлении лайка", friendId);
            return new NotFoundException("Пользователь с id " + friendId + "не найден");
        });

        List<Film> commonFilms = filmJdbcStorage.getCommonFilm(userId, friendId);
        log.info("Найдено {} общих фильмов", commonFilms.size());

        return genreJdbcStorage.getGenresByFilms(commonFilms).stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public List<FilmDto> searchFilms(String query, String by) {
        log.info("Поиск '{}' по критерию: {}", query, by);
        List<Film> searchResult = filmJdbcStorage.searchFilms(query, by);
        log.info("Найдено {} фильмов", searchResult.size());
        List<Long> originalOrder = searchResult.stream()
                .map(Film::getId)
                .toList();

        searchResult = genreJdbcStorage.getGenresByFilms(searchResult);
        searchResult = mpaJdbcStorage.getMpaByFilms(searchResult);
        searchResult = filmDirectorRepository.getDirectorByFilms(searchResult);

        Map<Long, Film> filmMap = searchResult.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));

        List<Film> orderedResult = originalOrder.stream()
                .map(filmMap::get)
                .filter(Objects::nonNull)
                .toList();

        return orderedResult.stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    private void saveFilmDirectorsIfPresent(NewFilmRequest request, Long filmId) {
        Optional.ofNullable(request.getDirectors())
                .filter(directors -> !directors.isEmpty())
                .map(directors -> directors.stream()
                        .map(Director::getId)
                        .collect(Collectors.toSet()))
                .ifPresent(directorIds -> {
                    filmDirectorRepository.saveFilmDirectors(filmId, directorIds);
                    log.info("Сохранены режиссеры для фильма {}: {}", filmId, directorIds);
                });
    }

    private void loadAllFilmRelations(Film film) {
        filmDirectorRepository.loadFilmDirectors(film);
        genreJdbcStorage.loadFilmGenres(film);
        mpaJdbcStorage.loadFilmMpa(film);
    }

    public void deleteById(Long id) {
        getFilmOrThrow(id);
        filmJdbcStorage.deleteById(id);
        log.info("Удален фильм с id: {}", id);
    }

    private void getFilmOrThrow(Long filmId) {
        filmJdbcStorage.findById(filmId).orElseThrow(() -> {
            log.warn("Фильм с id {} не найден", filmId);
            return new NotFoundException("Фильм не найден с id: " + filmId);
        });
    }
}