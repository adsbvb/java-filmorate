package ru.yandex.practicum.filmorate.dal;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.time.Instant;
import java.util.List;

@Repository
public class EventJdbcStorage extends BaseRepository<Event> implements EventRepository {
    private final String insertSqlQuery = """
            INSERT INTO events (event_time, user_id, event_type, operation, entity_id)
            VALUES (?,?,?,?,?);
            """;
    private final String readEventListQuery = """
            SELECT *
            FROM events
            WHERE user_id = ?
            """;

    @Autowired
    public EventJdbcStorage(JdbcTemplate jdbcTemplate, RowMapper<Event> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    @Override
    public void addEvent(long userId, String eventType, String operation, long entityId) {
        Long time = Instant.now().toEpochMilli();
        insert(insertSqlQuery, time, userId, eventType, operation, entityId);
    }

    @Override
    public List<Event> getUsersEventListOnId(long userId) {
        return findMany(readEventListQuery, userId);
    }
}