package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewReviewRequest {
    @NotNull(message = "Необходимо указать текст отзыва")
    String content;
    @NotNull(message = "Необходимо указать тип отзыва негативный/положительный")
    boolean isPositive;
    @NotNull(message = "Необходимо указать id пользователя")
    Long userId;
    @NotNull(message = "Необходимо указать id фильма")
    Long filmId;
}
