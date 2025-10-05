package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.UserJdbcStorage;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserJdbcStorage.class, UserRowMapper.class})
public class UserJdbcTest {

    @Autowired
    private UserJdbcStorage userJdbcStorage;

    @Test
    void testSaveUser() {
        User user = new User();
        user.setLogin("testLogin");
        user.setEmail("test@mail.ru");
        user.setName("testName");
        user.setBirthday(LocalDate.of(1999, 12, 12));

        userJdbcStorage.save(user);

        Optional<User> retrieved = userJdbcStorage.findById(user.getId());
        assertThat(retrieved).isPresent().hasValueSatisfying(u ->
                assertThat(u.getLogin()).isEqualTo("testLogin"));
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setLogin("testLogin");
        user.setEmail("test@mail.ru");
        user.setName("testName");
        user.setBirthday(LocalDate.of(1999, 12, 12));

        userJdbcStorage.save(user);
        user.setEmail("newTest@mail.ru");
        userJdbcStorage.update(user);

        Optional<User> retrieved = userJdbcStorage.findById(user.getId());
        assertThat(retrieved).isPresent().hasValueSatisfying(u ->
                assertThat(u.getEmail()).isEqualTo("newTest@mail.ru"));
    }
}
