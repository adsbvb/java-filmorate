package ru.yandex.practicum.filmorate.dal;

import jakarta.annotation.Nullable;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Optional<Review> findById(Long id);

    Review addReview(Review review);

    Review updateReview(Review review);

    boolean deleteReview(Long id);

    List<Review> findReviews(@Nullable Long filmId, int limit);

    void addLikeDislike(Long reviewId, Long userId, boolean isLike);

    void removeLikeDislike(Long reviewId, Long userId, boolean isLike);
}
