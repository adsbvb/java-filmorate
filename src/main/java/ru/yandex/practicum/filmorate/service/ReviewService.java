package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;

import java.util.List;

public interface ReviewService {
    ReviewDto addReview(NewReviewRequest request);

    ReviewDto updateReview(UpdateReviewRequest request);

    void deleteReviewById(Long id);

    ReviewDto getReviewById(Long id);

    List<ReviewDto> getReviewsByFilmId(Long filmId, Integer count);

    void addUserLike(Long id, Long userId);

    void addUserDislike(Long id, Long userId);

    void deleteUserLike(Long id, Long userId);

    void deleteUserDislike(Long id, Long userId);
}
