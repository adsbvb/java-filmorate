package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

public class UserControllerTest {

    UserController userController;

    @BeforeEach
    public void setUp() {
        userController = new UserController();
    }

    @Test
    public void testCreateValidUser() {
        User user = new User();
        user.setEmail("email@email.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1995,1,1));

        User createdUser = userController.create(user);

        assertNotNull(createdUser.getId());
        assertEquals(createdUser, user);
        assertEquals(1, userController.findAll().size());
    }

    @Test
    public void testCreateUserWithEmptyEmail() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1995,1,1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    public void testCreateUserWithIncorrectEmail() {
        User user = new User();
        user.setEmail("emailemail.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1995,1,1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    public void testCreateUserWithEmptyLogin() {
        User user = new User();
        user.setEmail("email@email.ru");
        user.setLogin("");
        user.setName("name");
        user.setBirthday(LocalDate.of(1995,1,1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    public void testCreateUserWithIncorrectLogin() {
        User user = new User();
        user.setEmail("email@email.ru");
        user.setLogin("login login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1995,1,1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    public void testCreateUserWithEmptyName() {
        User user = new User();
        user.setEmail("email@email.ru");
        user.setLogin("login");
        user.setName("");
        user.setBirthday(LocalDate.of(1995,1,1));

        User createdUser = userController.create(user);

        assertEquals(createdUser.getLogin(), createdUser.getName());
    }

    @Test
    public void testCreateUserWithIncorrectDateBirth() {
        User user = new User();
        user.setEmail("email@email.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setEmail("email@email.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1995,1,1));

        User createdUser = userController.create(user);

        User newUser = new User();
        newUser.setId(createdUser.getId());
        newUser.setEmail("newemail@email.ru");
        newUser.setLogin("newLogin");
        newUser.setName("newName");
        user.setBirthday(LocalDate.of(2000, 12, 12));

        User updatetUser = userController.update(newUser);

        assertEquals(updatetUser, newUser);
    }

    @Test
    public void testUpdateUserWithoutId() {
        User user = new User();
        user.setEmail("email@email.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1995,1,1));

        userController.create(user);

        User newUser = new User();
        newUser.setEmail("newemail@email.ru");
        newUser.setLogin("newLogin");
        newUser.setName("newName");
        user.setBirthday(LocalDate.of(2000, 12, 12));

        assertThrows(ValidationException.class, () -> userController.update(newUser));
    }

    @Test
    public void testUpdateUserWithInvalidId() {
        User user = new User();
        user.setEmail("email@email.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1995,1,1));

        userController.create(user);

        User newUser = new User();
        newUser.setId(9999L);
        newUser.setEmail("newemail@email.ru");
        newUser.setLogin("newLogin");
        newUser.setName("newName");
        user.setBirthday(LocalDate.of(2000, 12, 12));

        assertThrows(NotFoundException.class, () -> userController.update(newUser));
    }
}
