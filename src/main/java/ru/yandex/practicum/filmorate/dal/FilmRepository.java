package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmRepository {
    Optional<Film> findById(Long id);

    List<Film> findAll();

    Film save(Film film);

    Film update(Film film);

    boolean addLike(Long filmId, Long userId);

    boolean removeLike(Long filmId, Long userId);

    List<Film> getPopular(Integer genreId, Integer year, int count);

    List<Film> getCommonFilm(Long userId, Long friendId);
}
