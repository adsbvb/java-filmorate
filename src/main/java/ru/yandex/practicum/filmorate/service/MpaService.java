package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.dal.MpaJdbcStorage;

import java.util.List;

@Slf4j
@Service
public class MpaService {
    private final MpaJdbcStorage mpaJdbcStorage;

    @Autowired
    public MpaService(MpaJdbcStorage mpaJdbcStorage) {
        this.mpaJdbcStorage = mpaJdbcStorage;
    }

    public Mpa getMpaById(Integer id) {
        log.info("Получение MPA-рейтинг по id: {}", id);
        Mpa mpa = mpaJdbcStorage.findById(id).orElseThrow(() -> {
            log.warn("MPA-рейтинг с id {} не найден", id);
            return new NotFoundException("MPA-рейтинг не найден с id: " + id);
        });
        log.info("MPA-рейтинг найден: {}", mpa);
        return mpa;
    }

    public List<Mpa> getMpaAll() {
        log.info("Получение списка всех MPA-рейтингов");
        List<Mpa> mpaRatings = mpaJdbcStorage.findAll();
        log.info("Найдено {} MPA-рейтингов", mpaRatings.size());
        return mpaRatings;
    }
}
