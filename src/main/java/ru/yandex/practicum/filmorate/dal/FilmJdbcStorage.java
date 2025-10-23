package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FilmJdbcStorage extends BaseRepository<Film> implements FilmRepository {
    private static final String FIND_BY_ID_QUERY = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
            "FROM films f " + "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " + "WHERE f.id = ?";
    private static final String FIND_ALL_QUERY = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
            "FROM films f " + "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, mpa_id) " + "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String INSERT_FILM_LIKES_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKES_QUERY = "DELETE FROM film_likes WHERE film_id = ? and user_id = ?";

    private static final String FIND_COMMON_FILM_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name
            FROM films f
            JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
            JOIN (SELECT film_id, COUNT(user_id) AS like_count
            FROM film_likes
            GROUP BY film_id) fl ON f.id = fl.film_id
            WHERE f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?)
            AND f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?)
            ORDER BY fl.like_count DESC
            """;

    private static final String FIND_MOST_COMMON_LIKED_QUERY = """
            SELECT fl2.user_id
            FROM film_likes AS fl1
            JOIN film_likes AS fl2 ON fl1.film_id = fl2.film_id AND fl1.user_id != fl2.user_id
            WHERE fl1.user_id = ?
            GROUP BY fl2.user_id
            ORDER BY COUNT(fl2.film_id) DESC
            LIMIT 1;
            """;

    private static final String FIND_LIKED_BY_ONE_QUERY = """
            SELECT film_id FROM film_likes WHERE user_id = ?
            EXCEPT
            SELECT film_id FROM film_likes WHERE user_id = ?;
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
        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count != null && count > 0) {
            log.info("Лайк пользователя {} фильму {} уже существует", userId, filmId);
            return true;
        }
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
    public List<Film> getPopular(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder("""
        SELECT
            f.id,
            f.name,
            f.description,
            f.release_date,
            f.duration,
            f.mpa_id,
            m.name as mpa_name,
            COUNT(fl.user_id) AS likes_count
        FROM films f
        LEFT JOIN film_likes fl ON f.id = fl.film_id
        LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
        LEFT JOIN film_genres fg ON f.id = fg.film_id
        LEFT JOIN genres g ON fg.genre_id = g.genre_id
        WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (genreId != null && genreId > 0) {
            sql.append(" AND fg.genre_id = ?");
            params.add(genreId);
        }

        if (year != null && year > 0) {
            sql.append(" AND EXTRACT(YEAR FROM f.release_date) = ?");
            params.add(year);
        }

        sql.append(" GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name");
        sql.append(" ORDER BY COUNT(fl.user_id) DESC, f.id ASC");
        sql.append(" LIMIT ?");
        params.add(count);

        log.info("Executing popular films query: {}", sql);
        log.info("With params: {}", params);

        List<Film> result = jdbcTemplate.query(sql.toString(), mapper, params.toArray());
        log.info("Found {} popular films", result.size());
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
                            m.name AS mpa_name
                        FROM films f
                        LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                        LEFT JOIN film_likes l ON f.id = l.film_id
                        WHERE LOWER(f.name) LIKE LOWER(?)
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
                            m.name AS mpa_name
                        FROM films f
                        LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                        LEFT JOIN film_likes l ON f.id = l.film_id
                        LEFT JOIN film_directors fd ON f.id = fd.film_id
                        LEFT JOIN directors d ON d.director_id = fd.director_id
                        WHERE LOWER(d.director_name) LIKE LOWER(?)
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
            case "title,director", "director,title" -> {
                String sql = """
                        SELECT DISTINCT f.id,
                            f.name,
                            f.description,
                            f.release_date,
                            f.duration,
                            f.mpa_id,
                            m.name AS mpa_name
                        FROM films f
                        LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                        LEFT JOIN film_likes l ON f.id = l.film_id
                        LEFT JOIN film_directors fd ON f.id = fd.film_id
                        LEFT JOIN directors d ON d.director_id = fd.director_id
                        WHERE LOWER(d.director_name) LIKE LOWER(?) OR LOWER(f.name) LIKE LOWER(?)
                        ORDER BY f.id DESC
                        """;
                String param = "%" + query.toLowerCase() + "%";
                return findMany(sql, param, param);
            }
            default -> {
                String sql = """
                        SELECT f.id,
                            f.name,
                            f.description,
                            f.release_date,
                            f.duration,
                            f.mpa_id,
                            m.name AS mpa_name
                        FROM films f
                        LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                        LEFT JOIN film_likes l ON f.id = l.film_id
                        LEFT JOIN film_directors fd ON f.id = fd.film_id
                        LEFT JOIN directors d ON d.director_id = fd.director_id
                        WHERE LOWER(d.director_name) LIKE LOWER(?) OR LOWER(f.name) LIKE LOWER(?)
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

    private List<Film> findFilmsByIds(List<Long> filmsIds) {
        if (filmsIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = String.format(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                        "f.mpa_id, m.name AS mpa_name " +
                        "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                        "WHERE f.id IN (%s)",
                filmsIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","))
        );
        return jdbcTemplate.query(sql, mapper);
    }

    @Override
    public List<Film> getRecommendations(Long userId) {
        List<Long> similarUserIds = jdbcTemplate.queryForList(FIND_MOST_COMMON_LIKED_QUERY, Long.class, userId);
        if (similarUserIds.isEmpty()) {
            return Collections.emptyList();
        }
        Long similarUserId = similarUserIds.getFirst();
        List<Long> recommendationsFilmIds = jdbcTemplate.queryForList(FIND_LIKED_BY_ONE_QUERY, Long.class, similarUserId, userId);
        if (recommendationsFilmIds.isEmpty()) {
            return Collections.emptyList();
        }
        return findFilmsByIds(recommendationsFilmIds);
    }

    @Override
    public void deleteById(Long filmId) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, filmId);
    }
}