package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaJdbcStorage extends BaseRepository<Mpa> implements MpaRepository {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_ratings WHERE mpa_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_ratings";
    private static final String LOAD_FILM_MPA_QUERY = "Select name FROM mpa_ratings WHERE mpa_id = ?";

    public MpaJdbcStorage(JdbcTemplate jdbcTemplate, RowMapper<Mpa> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Optional<Mpa> findById(Integer id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public List<Mpa> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    protected void loadFilmMpa(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            String mpaName = jdbcTemplate.queryForObject(LOAD_FILM_MPA_QUERY, String.class, film.getMpa().getId());
            if (mpaName != null) {
                film.getMpa().setName(mpaName);
            }
        }
    }
}
