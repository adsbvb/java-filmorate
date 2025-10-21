package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
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
    public List<Film> getPopular(Integer genreId, Integer year, int count) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                        f.id,
                        f.name,
                        f.description,
                        f.release_date,
                        f.duration,
                        f.mpa_id,
                        m.name as mpa_name,
                        COUNT(l.user_id) AS likes_count
                    FROM films f
                    LEFT JOIN film_likes l ON f.id = l.film_id
                    LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                """);
        List<Object> params = new ArrayList<>();

        if (genreId != null && genreId > 0) {
            sql.append(" INNER JOIN film_genres fg ON f.id = fg.film_id");
        }

        sql.append(" WHERE 1=1");

        if (genreId != null && genreId > 0) {
            sql.append(" AND fg.genre_id = ?");
            params.add(genreId);
        }

        if (year != null && year > 0) {
            sql.append(" AND EXTRACT(YEAR FROM f.release_date) = ?");
            params.add(year);
        }
        sql.append(" GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name");
        sql.append(" ORDER BY likes_count DESC, f.id ASC");

        sql.append(" LIMIT ?");
        params.add(count);

        List<Film> result = jdbcTemplate.query(sql.toString(), mapper, params.toArray());
        return result;
    }

    @Override
    public List<Film> getCommonFilm(Long userId, Long friendId) {
        return findMany(FIND_COMMON_FILM_QUERY, userId, friendId);
    }

    @Override
    public List<Film> searchFilms(String query, String by) {
        switch (by) {
            case "title" -> {
                String sql = """
                        SELECT f.id,
                            f.name,
                            f.description,
                            f.release_date,
                            f.duration,
                            f.mpa_id,
                            m.name
                        FROM films f
                        LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                        LEFT JOIN film_likes l ON f.id = l.film_id
                        WHERE f.name LIKE ?
                        GROUP BY f.id,
                            f.name,
                            f.description,
                            f.release_date,
                            f.duration,
                            f.mpa_id,
                            m.name
                        ORDER BY COUNT(l.user_id) DESC
                        """;
                String title = "%" + query + "%";
                return findMany(sql, title);
            }
            case "director" -> {
                String sql = """
                        SELECT f.id,
                            f.name,
                            f.description,
                            f.release_date,
                            f.duration,
                            f.mpa_id,
                            m.name
                        FROM films f
                        LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                        LEFT JOIN film_likes l ON f.id = l.film_id
                        LEFT JOIN film_directors fd ON f.id = fd.film_id
                        LEFT JOIN directors d ON d.director_id = fd.director_id
                        WHERE d.director_name LIKE ?
                        GROUP BY f.id,
                            f.name,
                            f.description,
                            f.release_date,
                            f.duration,
                            f.mpa_id,
                            m.name
                        ORDER BY COUNT(l.user_id) DESC
                        """;
                String directorName = "%" + query + "%";
                return findMany(sql, directorName);
            }
			default -> {
                String sql = """
                        SELECT f.id,
                            f.name,
                            f.description,
                            f.release_date,
                            f.duration,
                            f.mpa_id,
                            m.name
                        FROM films f
                        LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                        LEFT JOIN film_likes l ON f.id = l.film_id
                        LEFT JOIN film_directors fd ON f.id = fd.film_id
                        LEFT JOIN directors d ON d.director_id = fd.director_id
                        WHERE d.director_name LIKE ? OR f.name LIKE ?
                        GROUP BY f.id,
                            f.name,
                            f.description,
                            f.release_date,
                            f.duration,
                            f.mpa_id,
                            m.name
                        ORDER BY COUNT(l.user_id) DESC
                        """;
                String param = "%" + query + "%";
                return findMany(sql, param, param);
            }
		}
    }

    private boolean isGenre(int genreId) {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM genres WHERE genre_id = ?", Integer.class, genreId);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
        return true;
    }

    @Override
    public void deleteById(Long filmId) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, filmId);
    }
}