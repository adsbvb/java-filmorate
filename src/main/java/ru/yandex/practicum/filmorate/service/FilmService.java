package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id).orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден."));
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film createFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public void likeFilm(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + "не найден.");
        }
        film.getLikedBy().add(userId);
        filmStorage.updateFilm(film);
    }

    public void unlikeFilm(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        if (!film.getLikedBy().contains(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " в списке лайков не найден.");
        }
        film.getLikedBy().remove(userId);
        filmStorage.updateFilm(film);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikedBy().size(), f1.getLikedBy().size()))
                .limit(count)
                .toList();
    }

}
