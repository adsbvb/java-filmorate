package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class FilmDirectorJdbcStorage implements FilmDirectorRepository {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper  filmRowMapper;

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
                directorIds.toArray()
        );

        film.setDirectors(new HashSet<>(directors));
    }

    @Override
    public List<Film> findFilmsByDirectorId(Long directorId, String sortBy) {
        List<Long> filmIds = jdbcTemplate.queryForList(
                FIND_FILM_IDS_BY_DIRECTOR_QUERY, Long.class, directorId
        );

        if (filmIds.isEmpty()) {
            return Collections.emptyList();
        }

        String filmsSql = buildFilmsQuery(filmIds.size(), sortBy);
        List<Film> films = jdbcTemplate.query(filmsSql, filmIds.toArray(), filmRowMapper);

        Map<Long, Set<Genre>> filmGenresMap = loadGenresForFilms(filmIds);

        Map<Long, Set<Director>> filmDirectorsMap = loadDirectorsForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(filmGenresMap.getOrDefault(film.getId(), Collections.emptySet()));
            film.setDirectors(filmDirectorsMap.getOrDefault(film.getId(), Collections.emptySet()));
        }

        return films;
    }

    private String buildFilmsQuery(int filmCount, String sortBy) {
        String placeholders = String.join(",", Collections.nCopies(filmCount, "?"));

        String baseQuery = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "f.mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "WHERE f.id IN (" + placeholders + ")";

        String orderBy = getOrderByClause(sortBy);

        return baseQuery + orderBy;
    }

    private Map<Long, Set<Genre>> loadGenresForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (" + placeholders + ")";

        Map<Long, Set<Genre>> filmGenresMap = new HashMap<>();

        jdbcTemplate.query(sql, filmIds.toArray(), (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            filmGenresMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
            return null;
        });

        return filmGenresMap;
    }

    private Map<Long, Set<Director>> loadDirectorsForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT fd.film_id, d.director_id, d.director_name " +
                "FROM film_directors fd JOIN directors d ON fd.director_id = d.director_id " +
                "WHERE fd.film_id IN (" + placeholders + ")";

        Map<Long, Set<Director>> filmDirectorsMap = new HashMap<>();

        jdbcTemplate.query(sql, filmIds.toArray(), (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");
            Director director = Director.builder()
                    .id(rs.getLong("director_id"))
                    .name(rs.getString("director_name"))
                    .build();
            filmDirectorsMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
            return null;
        });

        return filmDirectorsMap;
    }

    private String getOrderByClause(String sortBy) {
        if ("year".equals(sortBy)) {
            return " ORDER BY f.release_date";
        } else if ("likes".equals(sortBy)) {
            return " ORDER BY (SELECT COUNT(*) FROM film_likes l WHERE l.film_id = f.id) DESC";
        } else {
            return " ORDER BY f.id";
        }
    }
}
