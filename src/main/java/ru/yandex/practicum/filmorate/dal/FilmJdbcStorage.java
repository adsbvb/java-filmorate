package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;


@Component
public class FilmJdbcStorage extends BaseRepository<Film> implements FilmRepository {
    private static final String FIND_BY_ID_QUERY = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa " +
            "FROM films f " + "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " + "WHERE f.id = ?";
    private static final String FIND_ALL_QUERY = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa " +
            "FROM films f " + "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, mpa_id) " + "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String INSERT_FILM_LIKES_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKES_QUERY = "DELETE FROM film_likes WHERE film_id = ? and user_id = ?";

    private static final String FIND_POPULAR_FILM_QUERY =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                    "FROM films f " +
                    "JOIN film_likes l ON f.id = l.film_id " +
                    "JOIN film_genres g ON f.id = g.film_id " +
                    "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                    "ORDER BY COUNT(l.user_id) DESC " +
                    "LIMIT ?";

    public FilmJdbcStorage(JdbcTemplate jdbcTemplate, RowMapper<Film> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Optional<Film> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film save(Film film) {
        Long filmId = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                (film.getMpa() != null) ? film.getMpa().getId() : null
        );
        film.setId(filmId);
        return film;
    }

    @Override
    public Film update(Film film) {
        Long filmId = film.getId();
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                (film.getMpa() != null) ? film.getMpa().getId() : null,
                filmId
        );
        return film;
    }

    @Override
    public boolean addLike(Long filmId, Long userId) {
        return update(
                INSERT_FILM_LIKES_QUERY,
                filmId,
                userId
        );
    }

    @Override
    public boolean removeLike(Long filmId, Long userId) {
        return update(
                DELETE_LIKES_QUERY,
                filmId,
                userId
        );
    }

    @Override
    public List<Film> getPopular(Integer genreId, Integer year, int count) {
        String sql;
        if (genreId != null && year != null && isGenre(genreId)) {
            sql = """
                    SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                    FROM films f
                    JOIN film_genres g ON f.id = g.film_id
                    JOIN film_likes l ON f.id = l.film_id
                    WHERE g.genre_id = ? AND EXTRACT(YEAR FROM PARSEDATETIME(f.release_date, 'yyyy-MM-dd')) = ?
                    GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                    ORDER BY COUNT(l.user_id) DESC
                    LIMIT ?
                    """;
            return jdbcTemplate.query(sql, mapper, genreId, year, count);
        } else if (genreId != null && isGenre(genreId)) {
            sql = """
                      SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                      FROM films f
                      JOIN film_genres g ON f.id = g.film_id
                      JOIN film_likes l ON f.id = l.film_id
                      WHERE g.genre_id = ?
                      GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                      ORDER BY COUNT(l.user_id) DESC
                      LIMIT ?
                    """;
            return jdbcTemplate.query(sql, mapper, genreId, count);
        } else if (year != null) {
            sql = """
                    SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                    FROM films f
                    JOIN film_likes l ON f.id = l.film_id
                    WHERE EXTRACT(YEAR FROM f.release_date) = ?
                    GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                    ORDER BY COUNT(l.user_id) DESC
                    LIMIT ?
                    """;
            return jdbcTemplate.query(sql, mapper, year, count);
        }
        sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                FROM films f
                JOIN film_likes l ON f.id = l.film_id
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                ORDER BY COUNT(l.user_id) DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, mapper, count);
    }

    private boolean isGenre(int genreId) {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM genres WHERE genre_id = ?", Integer.class, genreId);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
        return true;
    }
}