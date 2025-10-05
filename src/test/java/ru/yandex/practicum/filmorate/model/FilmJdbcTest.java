package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.FilmJdbcStorage;
import ru.yandex.practicum.filmorate.dal.GenreJdbcStorage;
import ru.yandex.practicum.filmorate.dal.MpaJdbcStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmJdbcStorage.class, FilmRowMapper.class, GenreJdbcStorage.class, GenreRowMapper.class, MpaJdbcStorage.class, MpaRowMapper.class})
public class FilmJdbcTest {

    @Autowired
    private FilmJdbcStorage filmJdbcStorage;

    @Test
    void testSaveFilm() {
        Film film = new Film();
        film.setName("testName");
        film.setDescription("testDescription");
        film.setReleaseDate(LocalDate.of(2000,10,10));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("testMpa");
        film.setMpa(mpa);

        filmJdbcStorage.save(film);

        Optional<Film> retrieved = filmJdbcStorage.findById(film.getId());
        assertThat(retrieved).isPresent().hasValueSatisfying(f ->
                assertThat(f.getName()).isEqualTo("testName"));
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("testName");
        film.setDescription("testDescription");
        film.setReleaseDate(LocalDate.of(2000,10,10));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("testMpa");
        film.setMpa(mpa);

        filmJdbcStorage.save(film);
        film.setDescription("newTestDescription");
        filmJdbcStorage.update(film);

        Optional<Film> retrieved = filmJdbcStorage.findById(film.getId());
        assertThat(retrieved).isPresent().hasValueSatisfying(f ->
                assertThat(f.getDescription()).isEqualTo("newTestDescription"));
    }
}
