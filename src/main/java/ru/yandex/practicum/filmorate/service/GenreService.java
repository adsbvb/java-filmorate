package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.dal.GenreJdbcStorage;

import java.util.List;

@Slf4j
@Service
public class GenreService {
    private final GenreJdbcStorage genreJdbcStorage;

    @Autowired
    public GenreService(GenreJdbcStorage genreJdbcStorage) {
        this.genreJdbcStorage = genreJdbcStorage;
    }

    public Genre getGenreById(Integer id) {
        log.info("Получение жанра по id: {}", id);
        Genre genre = genreJdbcStorage.findById(id).orElseThrow(() -> {
            log.warn("Жанр с id {} не найден", id);
            return new NotFoundException("Жанр не найден с id: " + id);
        });
        log.info("Жанр найден: {}", genre);
        return genre;
    }

    public List<Genre> getAllGenres() {
        log.info("Получение списка всех фильмов");
        List<Genre> genres = genreJdbcStorage.findAll();
        log.info("Найдено {} жанров", genres.size());
        return genres;
    }
}
