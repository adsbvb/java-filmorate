package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmDirectorJdbcStorage implements FilmDirectorRepository {
    private final JdbcTemplate jdbcTemplate;
    private final DirectorJdbcStorage directorJdbcStorage;
    private final FilmJdbcStorage filmJdbcStorage;

    private static final String INSERT_FILM_DIRECTORS_QUERY =
            "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS_QUERY =
            "DELETE FROM film_directors WHERE film_id = ?";
    private static final String FIND_DIRECTOR_IDS_BY_FILM_QUERY =
            "SELECT director_id FROM film_directors WHERE film_id = ?";
    private static final String FIND_FILM_IDS_BY_DIRECTOR_QUERY =
            "SELECT film_id FROM film_directors WHERE director_id = ?";

    @Override
    public void saveFilmDirectors(Long filmId, Set<Long> directorIds) {
        if (directorIds != null && !directorIds.isEmpty()) {
            for (Long directorId : directorIds) {
                jdbcTemplate.update(INSERT_FILM_DIRECTORS_QUERY, filmId, directorId);
            }
        }
    }

    @Override
    public void updateFilmDirectors(Long filmId, Set<Long> directorIds) {
        jdbcTemplate.update(DELETE_FILM_DIRECTORS_QUERY, filmId);
        saveFilmDirectors(filmId, directorIds);
    }

    @Override
    public Set<Long> findDirectorIdsByFilmId(Long filmId) {
        List<Long> directorIds = jdbcTemplate.queryForList(
                FIND_DIRECTOR_IDS_BY_FILM_QUERY, Long.class, filmId
        );
        return new HashSet<>(directorIds);
    }

    @Override
    public void loadFilmDirectors(Film film) {
        Set<Long> directorIds = findDirectorIdsByFilmId(film.getId());
        Set<Director> directors = directorIds.stream()
                .map(id -> directorJdbcStorage.findById(id).orElse(null))
                .filter(director -> director != null)
                .collect(Collectors.toSet());
        film.setDirectors(directors);
    }

    @Override
    public List<Film> findFilmsByDirectorId(Long directorId, String sortBy) {
        List<Long> filmIds = jdbcTemplate.queryForList(
                FIND_FILM_IDS_BY_DIRECTOR_QUERY, Long.class, directorId
        );

        List<Film> films = filmIds.stream()
                .map(id -> filmJdbcStorage.findById(id).orElse(null))
                .filter(film -> film != null)
                .collect(Collectors.toList());

        if ("year".equals(sortBy)) {
            films.sort((f1, f2) -> f1.getReleaseDate().compareTo(f2.getReleaseDate()));
        } else if ("likes".equals(sortBy)) {
            films.sort((f1, f2) -> Integer.compare(f2.getLikedBy().size(), f1.getLikedBy().size()));
        }

        return films;
    }
}
