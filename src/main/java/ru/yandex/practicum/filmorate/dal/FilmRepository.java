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

    List<Film> getPopular(int count, Integer genreId, Integer year);

    List<Film> getCommonFilm(Long userId, Long friendId);

    List<Film> searchFilms(String query, String by);

    List<Film> getRecommendations(Long userId);

    void deleteById(Long filmId);
}
