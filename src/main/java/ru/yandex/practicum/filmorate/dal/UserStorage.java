package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    User save(User user);

    User update(User user);

    void addFriend(Long userId, Long friendId);

    boolean removeFriend(Long userId, Long friendId);

    List<User> getFriends(Long userId);

    List<User> getCommonFriends(Long userID, Long otherId);

    void deleteUser(Long userID);

    void deleteUserFriends(Long userId);

    void deleteUserLikes(Long userId);

    void deleteUserEvents(Long userId);

}
