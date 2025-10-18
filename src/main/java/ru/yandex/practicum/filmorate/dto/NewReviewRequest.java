package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewReviewRequest {
    @NotNull(message = "Необходимо указать текст отзыва")
    String content;
    @NotNull(message = "Необходимо указать тип отзыва негативный/положительный")
    Boolean isPositive;
    @NotNull(message = "Необходимо указать id пользователя")
    //@Positive --- не проходит тест PostMan из-за кода ошибки
    Long userId;
    @NotNull(message = "Необходимо указать id фильма")
    //@Positive --- не проходит тест PostMan из-за кода ошибки
    Long filmId;
}
