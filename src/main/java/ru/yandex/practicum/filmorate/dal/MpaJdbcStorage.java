package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public void loadFilmMpa(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            String mpaName = jdbcTemplate.queryForObject(LOAD_FILM_MPA_QUERY, String.class, film.getMpa().getId());
            if (mpaName != null) {
                film.getMpa().setName(mpaName);
            }
        }
    }

    public List<Film> getMpaByFilms(List<Film> films) {
        List<Long> filmsId = films.stream().map(Film::getId).toList();
        Map<Long, Film> filmsMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film));

        if (filmsId.isEmpty()) {
            return new ArrayList<>();
        }

        String idsStr = filmsId.stream().map(String::valueOf).collect(Collectors.joining(","));
        String filmMpaSql = """
                SELECT f.id, m.mpa_id, m.name
                FROM films f 
                JOIN mpa_ratings m ON f.mpa_id = m.mpa_id 
                WHERE f.id IN (""" + idsStr + ")";

        jdbcTemplate.query(filmMpaSql, rs -> {
            Long filmId = rs.getLong("id");
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("name"));

            Film film = filmsMap.get(filmId);
            if (film != null) {
                film.setMpa(mpa);
            }
        });

        return filmsMap.values().stream().toList();
    }
}
