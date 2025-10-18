package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@Slf4j
@Service
public class ImplReviewService implements ReviewService {
    private final ReviewRepository reviewJdbcStorage;

    @Autowired
    public ImplReviewService(ReviewRepository reviewJdbcStorage) {
        this.reviewJdbcStorage = reviewJdbcStorage;
    }

    @Override
    public ReviewDto addReview(NewReviewRequest request) {
        log.info("Add new review: {}", request);
        Review review = ReviewMapper.mapToReview(request);
        review.setUseful(0);
        review = reviewJdbcStorage.addReview(review);
        log.info("Review successfully created with id: {}", review.getReviewId());
        return ReviewMapper.mapToReviewDto(review);
    }

    @Override
    public ReviewDto updateReview(UpdateReviewRequest request) {
        log.info("Updating review with id: {}", request.getReviewId());
        Review updatedReview = reviewJdbcStorage.findById(request.getReviewId())
                .map(review -> {
                    log.debug("Updating review fields: {}", review);
                    return ReviewMapper.updateReviewFields(review, request);
                })
                .orElseThrow(() -> {
                    log.warn("Review with id {} not found for update", request.getReviewId());
                    return new NotFoundException("Not found for update review with id {}: " + request.getReviewId());
                });
        updatedReview = reviewJdbcStorage.updateReview(updatedReview);
        log.info("Review with id {} updated successfully", updatedReview.getReviewId());
        return ReviewMapper.mapToReviewDto(updatedReview);
    }

    @Override
    public void deleteReviewById(Long id) {
        boolean deleted = reviewJdbcStorage.deleteReview(id);
        if (!deleted) {
            throw new NotFoundException("Review with id " + id + " not found");
        }
        log.info("Review with id {} deleted successfully", id);
    }

    @Override
    public ReviewDto getReviewById(Long id) {
        Review review = reviewJdbcStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Review with id {} not found", id);
                    return new NotFoundException("Not found for update review with id {}: " + id);
                });
        return ReviewMapper.mapToReviewDto(review);
    }

    @Override
    public List<ReviewDto> getReviewsByFilmId(Long filmId, Integer count) {
        List<Review> reviews;
        reviews = reviewJdbcStorage.findReviews(filmId, count);
        return reviews.stream()
                .map(ReviewMapper::mapToReviewDto)
                .toList();
    }

    @Override
    public void addUserLike(Long id, Long userId) {
        reviewJdbcStorage.addLikeDislike(id, userId, true);
    }

    @Override
    public void addUserDislike(Long id, Long userId) {
        reviewJdbcStorage.addLikeDislike(id, userId, false);
    }

    @Override
    public void deleteUserLike(Long id, Long userId) {
        reviewJdbcStorage.removeLikeDislike(id, userId, true);
    }

    @Override
    public void deleteUserDislike(Long id, Long userId) {
        reviewJdbcStorage.removeLikeDislike(id, userId, false);
    }


}
