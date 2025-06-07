package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;

@SpringBootTest
public class UserControllerTest {

    @Autowired
    private UserController userController;

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
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
    }

    @Test
    public void testCreateUserWithEmptyEmail() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1995,1,1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertEquals("Электронная почта не может быть пустой", violations.iterator().next().getMessage());
    }

    @Test
    public void testCreateUserWithIncorrectEmail() {
        User user = new User();
        user.setEmail("emailemail.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1995,1,1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertEquals("Некорректный адрес электронной почты", violations.iterator().next().getMessage());
    }

    @Test
    public void testCreateUserWithEmptyLogin() {
        User user = new User();
        user.setEmail("email@email.ru");
        user.setLogin("");
        user.setName("name");
        user.setBirthday(LocalDate.of(1995,1,1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
    }

    @Test
    public void testCreateUserWithIncorrectLogin() {
        User user = new User();
        user.setEmail("email@email.ru");
        user.setLogin("login login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1995,1,1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertEquals("Логин не должен содержать пробелы", violations.iterator().next().getMessage());
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

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
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
