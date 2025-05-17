package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

public class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    public void setUp() {
        filmController = new FilmController();
    }

    @Test
    public void testCreateValidFilm() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(240);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals(createdFilm, film);
        assertEquals(1, filmController.findAll().size());
    }

    @Test
    public void testCreateFilmWithEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(240);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    public void testCreateFilmWithLongDescription() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(240);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    public void testCreateFilmWithWrongReleaseDate() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("description");
        LocalDate cinemaBirthday = LocalDate.of(1895, 12, 28);
        film.setReleaseDate(cinemaBirthday.minusDays(1));
        film.setDuration(240);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    public void testCreateFilmWithNotPositiveDuration() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(240);

        Film createdFilm = filmController.create(film);

        Film newFilm = new Film();
        newFilm.setId(createdFilm.getId());
        newFilm.setName("newName");
        newFilm.setDescription("newDescription");
        newFilm.setReleaseDate(LocalDate.of(1950, 10,10));
        newFilm.setDuration(120);

        Film updatedFilm = filmController.update(newFilm);

        assertEquals(newFilm, updatedFilm);
    }

    @Test
    public void testUpdateFilmWithoutId() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(240);

        filmController.create(film);

        Film newFilm = new Film();
        newFilm.setName("newName");
        newFilm.setDescription("newDescription");
        newFilm.setReleaseDate(LocalDate.of(1950, 10,10));
        newFilm.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.update(newFilm));
    }

    @Test
    public void testUpdateFilmWithInvalidId() {
        Film film = new Film();
        film.setName("name");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(240);

        filmController.create(film);

        Film newFilm = new Film();
        newFilm.setId(9999L);
        newFilm.setName("newName");
        newFilm.setDescription("newDescription");
        newFilm.setReleaseDate(LocalDate.of(1950, 10,10));
        newFilm.setDuration(120);

        assertThrows(NotFoundException.class, () -> filmController.update(newFilm));
    }

}
