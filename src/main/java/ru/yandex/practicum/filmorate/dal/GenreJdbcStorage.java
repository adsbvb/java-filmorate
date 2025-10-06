package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Repository
public class GenreJdbcStorage extends BaseRepository<Genre> implements GenreRepository {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String INSERT_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String LOAD_FILM_GENRES_QUERY = "SELECT g.genre_id, g.name " + "FROM genres g " +
            "JOIN film_genres fg ON g.genre_id = fg.genre_id " + "WHERE fg.film_id = ?";

    public GenreJdbcStorage(JdbcTemplate jdbcTemplate, RowMapper<Genre> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public void saveFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Object[]> batchList = new ArrayList<>();
            for (Genre genre : film.getGenres()) {
                batchList.add(new Object[] {film.getId(), genre.getId()});
            }
            jdbcTemplate.batchUpdate(INSERT_QUERY, batchList);
        }
    }

    public void updateFilmGenres(Film film) {
        jdbcTemplate.update(DELETE_QUERY, film.getId());
        saveFilmGenres(film);
    }

    public void loadFilmGenres(Film film) {
        Set<Genre> genres = new HashSet<>(jdbcTemplate.query(LOAD_FILM_GENRES_QUERY, mapper, film.getId()));
        film.setGenres(genres);
    }
}
