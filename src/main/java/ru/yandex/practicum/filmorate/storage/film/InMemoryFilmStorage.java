package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Optional<Film> getFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id фильма должен быть указан.");
        }
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            return film;
        }
        throw new NotFoundException("Фильм с id=" + film.getId() + " не найден.");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
