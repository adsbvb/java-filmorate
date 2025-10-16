package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

@Component
public class DirectorJdbcStorage extends BaseRepository<Director> implements DirectorRepository{

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE director_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors ORDER BY director_id";
    private static final String INSERT_QUERY = "INSERT INTO directors (director_name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE directors SET director_name = ? WHERE director_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE director_id = ?";

    public DirectorJdbcStorage(JdbcTemplate jdbcTemplate, DirectorRowMapper directorRowMapper) {
        super(jdbcTemplate, directorRowMapper);
    }

    @Override
    public Optional<Director> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Collection<Director> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Director create(Director director) {
        Long directorId = insert(
                INSERT_QUERY,
                director.getName()
        );
        director.setId(directorId);
        return director;
    }

    @Override
    public void update(Director director) {
        update(
                UPDATE_QUERY,
                director.getName(),
                director.getId()
        );
    }

    @Override
    public void delete(Long id) {
        update(DELETE_QUERY, id);
    }

}
