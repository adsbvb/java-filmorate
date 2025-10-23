package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateDirectorRequest {
    @NotNull
    Long id;
    String name;

    public boolean hasName() {
        return name != null && !name.isEmpty();
    }
}
