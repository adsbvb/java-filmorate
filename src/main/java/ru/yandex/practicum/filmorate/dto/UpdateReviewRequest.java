package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateReviewRequest {
    Long reviewId;
    @NotNull(message = "Необходимо указать текст отзыва")
    String content;
    @NotNull(message = "Необходимо указать тип отзыва негативный/положительный")
    Boolean isPositive;

    public boolean hasContent() {
        return ! (content == null || content.isBlank());
    }

    public boolean hasPositive() {
        return ! (isPositive == null);
    }
}
