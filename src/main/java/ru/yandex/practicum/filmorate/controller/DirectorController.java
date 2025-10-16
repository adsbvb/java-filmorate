package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@Slf4j
@RequiredArgsConstructor
@Validated
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<DirectorDto>  findAll() {
        log.trace("Получен запрос на получение коллекции режиссеров");
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public DirectorDto findById(@PathVariable @Positive long id) {
        log.trace("Получен запрос на получение режиссера с номером {}", id);
        return directorService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectorDto create(@RequestBody @Valid NewDirectorRequest request) {
        log.trace("Получен запрос на создание нового режиссера: {}", request);
        return directorService.create(request);
    }

    @PutMapping
    public DirectorDto update(@RequestBody @Valid UpdateDirectorRequest request) {
        log.trace("Получен запрос на обнавление данных режиссера: {}", request);
        return directorService.update(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive long id) {
        log.trace("Получен запрос на удаление режиссера с номером {}", id);
        directorService.delete(id);
    }
}
