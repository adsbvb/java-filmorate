package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmDirectorRepository {
    void saveFilmDirectors(Long filmId, Set<Long> directorIds);

    void updateFilmDirectors(Long filmId, Set<Long> directorIds);

    Set<Long> findDirectorIdsByFilmId(Long filmId);

    void loadFilmDirectors(Film film);

    List<Film> findFilmsByDirectorId(Long directorId, String sortBy);
}
