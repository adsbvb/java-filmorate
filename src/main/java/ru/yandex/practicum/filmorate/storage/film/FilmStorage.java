package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Optional<Film> getFilmById(Long id);

    List<Film> getAllFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);
}
