package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;

    public Collection<DirectorDto> findAll() {
        return directorRepository.findAll()
                .stream()
                .map(DirectorMapper::toDirectorDto)
                .toList();
    }

    public DirectorDto findById(long id) {
        return directorRepository.findById(id)
                .map(DirectorMapper::toDirectorDto)
                .orElseThrow(() -> new NotFoundException("Режиссер с номером %d не найден".formatted(id)));
    }

    public DirectorDto create(NewDirectorRequest request) {
        Director director = DirectorMapper.toDirector(request);
        director = directorRepository.create(director);
        log.info("Режиссер с номером {} создан", director.getId());
        return DirectorMapper.toDirectorDto(director);
    }

    public DirectorDto update(UpdateDirectorRequest request) {
        Director director = directorRepository.findById(request.getId()).orElseThrow(
                () -> new NotFoundException("Режиссер с номером %d не найден".formatted(request.getId()))
        );
        director = DirectorMapper.updateDirector(director, request);
        directorRepository.update(director);
        log.info("Режиссер с номером {} обновлен", director.getId());
        return DirectorMapper.toDirectorDto(director);
    }

    public void delete(long id) {
        directorRepository.delete(id);
        log.info("Режиссер с номером {} удален", id);
    }
}
