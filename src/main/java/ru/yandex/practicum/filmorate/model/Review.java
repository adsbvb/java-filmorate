package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {
    Long reviewId;
    String content;
    Boolean isPositive;
    Long userId;
    Long filmId;
    Integer useful;
}
