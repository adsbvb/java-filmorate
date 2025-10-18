package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorRepository {
    Collection<Director> findAll();

    Optional<Director> findById(Long id);

    Director create(Director director);

    void update(Director director);

    void delete(Long id);
}