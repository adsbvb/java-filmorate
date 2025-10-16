package ru.yandex.practicum.filmorate.dal;

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

    private static final String FIND_POPULAR_FILM_QUERY = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
            "FROM films f " + "JOIN film_likes l ON f.id = l.film_id " + "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
            "ORDER BY COUNT(l.user_id) DESC " + "LIMIT ?";

    private static final String FIND_COMMON_FILM_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
            FROM films f
            JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
            JOIN (SELECT film_id, COUNT(user_id) AS like_count
                FROM film_likes
                GROUP BY film_id) fl ON f.id = fl.film_id
            WHERE f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?)
            AND f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?)
            ORDER BY fl.like_count DESC
            """;

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
    public List<Film> getPopular(int count) {
        return findMany(FIND_POPULAR_FILM_QUERY, count);
    }

    @Override
    public List<Film> getCommonFilm(Long userId, Long friendId) {
        return findMany(FIND_COMMON_FILM_QUERY, userId, friendId);
    }


}
