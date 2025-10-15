package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {
    Long reviewId;
    String content;
    boolean isPositive;
    Long userId;
    Long filmId;
    int useful;
}
