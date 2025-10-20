package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventRepository {
    void addEvent(long userId, String eventType, String operation, long entityId);

    List<Event> getUsersEventListOnId(long userId);
}
