package ru.yandex.practicum.filmorate.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;

@UtilityClass
public class DirectorMapper {
    public DirectorDto toDirectorDto (Director director){
        return new DirectorDto (director.getId(), director.getName());
    }

    public Director toDirector (NewDirectorRequest directorDto){
        return Director.builder()
                .name(directorDto.getName())
                .build();
    }

    public Director updateDirector (Director director, UpdateDirectorRequest request){
        if (request.hasName()) {
            director.setName(request.getName());
        }

        return director;
    }
}
