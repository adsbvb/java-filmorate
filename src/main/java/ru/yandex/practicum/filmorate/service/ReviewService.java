package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    ReviewDto addReview(NewReviewRequest request);

    ReviewDto updateReview(UpdateReviewRequest request);

    void deleteReview(Long id);

    Optional<ReviewDto> getReviewById(Long id);

    List<ReviewDto> getReviews(Long filmId, Integer count);

    void likeReview(Long id, Long userId);

    void dislikeReview(Long id, Long userId);

    void removeLike(Long id, Long userId);

    void removeDislike(Long id, Long userId);
}
