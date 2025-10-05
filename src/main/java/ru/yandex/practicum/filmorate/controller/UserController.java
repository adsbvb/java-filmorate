package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUserById(@PathVariable("id") Long userId) {
        log.info("Получен запрос получение пользователя с id: {}", userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers() {
        log.info("Получен запрос на получение списка всех пользователей");
        return userService.getAllUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody NewUserRequest newUserRequest) {
        log.info("Получен запрос на создания пользователя {}", newUserRequest.getLogin());
        return userService.createUser(newUserRequest);
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody UpdateUserRequest request) {
        log.info("Получен запрос на обновление пользователя с id: {}", request.getId());
        return userService.updateUser(request);
    }

    @PutMapping("/{id}/friends/{friend_id}")
    public void addFriend(@PathVariable("id") Long userId, @PathVariable("friend_id") Long friendId) {
        log.info("Получен запрос на добавление пользователя {} в друзья к пользователю {}", friendId, userId);
        userService.addFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getFriends(@PathVariable("id") Long userId) {
        log.info("Получен запрос на получения списка друзей пользователя id: {}", userId);
        return userService.getFriends(userId);
    }

    @DeleteMapping("/{id}/friends/{friend_id}")
    public boolean removeFriend(@PathVariable("id") Long userId, @PathVariable("friend_id") Long friendId) {
        log.info("Получен запрос на удаление пользователя {} из списка друзей пользователя {}", friendId, userId);
        return userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends/common/{other_id}")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getCommonFriends(@PathVariable("id") Long userId, @PathVariable("other_id") Long otherUserId) {
        log.info("Получен запрос на получение списка общих друзей пользователя {} и {}", userId, otherUserId);
        return userService.getCommonFriends(userId, otherUserId);
    }
}
