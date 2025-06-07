package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    User addUser(User user);
    User updateUser(User user);
}
