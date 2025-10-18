package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class FilmDirectorJdbcStorage implements FilmDirectorRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_FILM_DIRECTORS_QUERY =
            "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS_QUERY =
            "DELETE FROM film_directors WHERE film_id = ?";
    private static final String FIND_DIRECTOR_IDS_BY_FILM_QUERY =
            "SELECT director_id FROM film_directors WHERE film_id = ?";
    private static final String FIND_FILMS_BY_DIRECTOR_WITH_DETAILS_QUERY = """
            SELECT 
                f.id, f.name, f.description, f.release_date, f.duration, 
                f.mpa_id, m.name AS mpa_name,
                g.genre_id, g.name AS genre_name,
                d.director_id, d.director_name,
                (SELECT COUNT(*) FROM film_likes l WHERE l.film_id = f.id) AS likes_count
            FROM films f
            JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
            JOIN film_directors fd ON f.id = fd.film_id
            JOIN directors d ON fd.director_id = d.director_id
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.genre_id
            WHERE d.director_id = ?
            """;

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

        if (directorIds.isEmpty()) {
            film.setDirectors(Collections.emptySet());
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(directorIds.size(), "?"));
        String sql = "SELECT director_id, director_name FROM directors WHERE director_id IN (" + placeholders + ")";

        List<Director> directors = jdbcTemplate.query(sql,
                (rs, rowNum) -> Director.builder()
                        .id(rs.getLong("director_id"))
                        .name(rs.getString("director_name"))
                        .build(),
                directorIds.toArray()  // параметры передаем отдельно
        );

        film.setDirectors(new HashSet<>(directors));
    }

    @Override
    public List<Film> findFilmsByDirectorId(Long directorId, String sortBy) {
        String orderByClause = getOrderByClause(sortBy);
        String fullQuery = FIND_FILMS_BY_DIRECTOR_WITH_DETAILS_QUERY + orderByClause;

        List<Film> filmsWithDuplicates = jdbcTemplate.query(fullQuery, this::mapRowToFilm, directorId);

        return mergeDuplicateFilms(filmsWithDuplicates);
    }

    private String getOrderByClause(String sortBy) {
        if (sortBy.equals("year")) {
            return " ORDER BY f.release_date";
        } else if (sortBy.equals("likes")) {
            return " ORDER BY likes_count DESC";
        } else {
            return " ORDER BY f.id";
        }
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        if (rs.getObject("mpa_id") != null) {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }

        if (rs.getObject("genre_id") != null) {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("genre_name"));
            film.getGenres().add(genre);
        }

        if (rs.getObject("director_id") != null) {
            Director director = Director.builder()
                    .id(rs.getLong("director_id"))
                    .name(rs.getString("director_name"))
                    .build();
            film.getDirectors().add(director);
        }

        return film;
    }

    private List<Film> mergeDuplicateFilms(List<Film> filmsWithDuplicates) {
        Map<Long, Film> filmMap = new LinkedHashMap<>();

        for (Film film : filmsWithDuplicates) {
            Long filmId = film.getId();
            if (filmMap.containsKey(filmId)) {
                Film existingFilm = filmMap.get(filmId);
                existingFilm.getGenres().addAll(film.getGenres());
            } else {
                filmMap.put(filmId, film);
            }
        }

        return new ArrayList<>(filmMap.values());
    }
}
