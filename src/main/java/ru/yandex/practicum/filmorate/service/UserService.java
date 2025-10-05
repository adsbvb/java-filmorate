package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.dal.UserJdbcStorage;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserJdbcStorage userJdbcStorage;

    @Autowired
    public UserService(UserJdbcStorage userJdbcStorage) {
        this.userJdbcStorage = userJdbcStorage;
    }

    public UserDto getUserById(Long userId) {
        log.info("Поиск пользователя по id: {}", userId);
        UserDto userDto = userJdbcStorage.findById(userId)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден с id: {}", userId);;
                    return new NotFoundException("Пользователь не найден с id: " + userId);
                });
        log.info("Найден пользователь: {}", userDto);
        return userDto;
    }

    public List<UserDto> getAllUsers() {
        log.info("Поиск всех пользователей");
        List<UserDto> users = userJdbcStorage.findAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .toList();
        log.info("Всего пользователей: {}", users.size());
        return users;
    }

    public UserDto createUser(NewUserRequest request) {
        log.info("Создание нового пользователя с данными: {}", request);
        if (request.getName() == null || request.getName().isBlank()) {
            request.setName(request.getLogin());
            log.info("Имя пользователя пустое, установлено как логин: {}", request.getLogin());
        }
        Optional<User> alreadyExistsUser = userJdbcStorage.findByEmail(request.getEmail());
        if (alreadyExistsUser.isPresent()) {
            log.warn("Попытка создать пользователя с уже существующим email: {}", request.getEmail());
            throw new DuplicatedDataException("Данный email уже используется: " + request.getEmail());
        }
        User user = UserMapper.mapToUser(request);
        user = userJdbcStorage.save(user);
        log.info("Создан пользователь: {}", user);
        return UserMapper.mapToUserDto(user);
    }

    public UserDto updateUser(UpdateUserRequest request) {
        log.info("Обновление пользователя с ID: {}", request.getId());
        User updatedUser = userJdbcStorage.findById(request.getId())
                .map(user -> {
                    log.info("Найден пользователь для обновления: {}", user);
                    return UserMapper.updateUserFields(user, request);
                })
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден для обновления с id: {}", request.getId());
                    return new NotFoundException("Пользователь не найден для обновления с id: " + request.getId());
                });
        updatedUser = userJdbcStorage.update(updatedUser);
        log.info("Пользователь после обновления: {}", updatedUser);
        return UserMapper.mapToUserDto(updatedUser);
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Добавление друга с id: {} для пользователя с id: {}", friendId, userId);
        userJdbcStorage.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь не найден с id: {}", userId);
            return new NotFoundException("Пользователь не найден с id: " + userId);
        });
        userJdbcStorage.findById(friendId).orElseThrow(() -> {
            log.warn("Пользователь не найден с id: {}", friendId);
            return new NotFoundException("Пользователь не найден с id: " + friendId);
        });
        userJdbcStorage.addFriend(userId, friendId);
        log.info("Друг с id: {} успешно добавлен к пользователю с id: {}", friendId, userId);
    }

    public boolean removeFriend(Long userId, Long friendId) {
        log.info("Удаление друга с id: {} у пользователя с id: {}", friendId, userId);
        userJdbcStorage.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь не найден с id: {}", userId);;
            return new NotFoundException("Пользователь не найден с id: " + userId);
        });
        userJdbcStorage.findById(friendId).orElseThrow(() -> {
            log.warn("Пользователь не найден с id: {}", friendId);;
            return new NotFoundException("Пользователь не найден с id: " + friendId);
        });
        return userJdbcStorage.removeFriend(userId, friendId);
    }

    public List<UserDto> getFriends(Long userId) {
        log.info("Получение друзей пользователя с id: {}", userId);
        User user = userJdbcStorage.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь не найден с id: {}", userId);;
            return new NotFoundException("Пользователь не найден с id: " + userId);
        });
        List<User> friends = userJdbcStorage.getFriends(userId);
        log.info("Пользователь с id: {} имеет {} друзей", userId, friends.size());
        return friends.stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getCommonFriends(Long userId, Long otherUserId) {
        log.info("Получение общих друзей между пользователями {} и {}", userId, otherUserId);
        userJdbcStorage.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь не найден с id: {}", userId);;
            return new NotFoundException("Пользователь не найден с id: " + userId);
        });
        userJdbcStorage.findById(otherUserId).orElseThrow(() -> {
            log.warn("Пользователь не найден с id: {}", otherUserId);;
            return new NotFoundException("Пользователь не найден с id: " + otherUserId);
        });
        List<User> commonFriends = userJdbcStorage.getCommonFriends(userId, otherUserId);
        log.info("Общих друзей найдено: {}", commonFriends.size());
        return commonFriends.stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }
}
