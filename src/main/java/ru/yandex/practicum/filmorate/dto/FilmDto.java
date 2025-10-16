package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;
    Set<Genre> genres = new HashSet<>();
    Mpa mpa;
    Set<Long> likedBy = new HashSet<>();
    Set<DirectorDto> directors = new HashSet<>(); //удалил модификатор private добавил аннотацию FiledDefaults
}
