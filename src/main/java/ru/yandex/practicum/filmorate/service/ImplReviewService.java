package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.EventRepository;
import ru.yandex.practicum.filmorate.dal.FilmJdbcStorage;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dal.UserJdbcStorage;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@Slf4j
@Service
public class ImplReviewService implements ReviewService {
    private final ReviewRepository reviewJdbcStorage;
    private final EventRepository eventRepository;
    private final UserJdbcStorage userJdbcStorage;
    private final FilmJdbcStorage filmJdbcStorage;

    @Autowired
    public ImplReviewService(ReviewRepository reviewJdbcStorage, EventRepository eventRepository,
                             UserJdbcStorage userJdbcStorage, FilmJdbcStorage filmJdbcStorage) {
        this.reviewJdbcStorage = reviewJdbcStorage;
        this.eventRepository = eventRepository;
        this.userJdbcStorage = userJdbcStorage;
        this.filmJdbcStorage = filmJdbcStorage;
    }

    @Override
    public ReviewDto addReview(NewReviewRequest request) {
        log.info("Add new review: {}", request);

        userJdbcStorage.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + request.getUserId()));
        filmJdbcStorage.findById(request.getFilmId())
                .orElseThrow(() -> new NotFoundException("Film not found with id: " + request.getFilmId()));

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ValidationException("Content cannot be empty");
        }

        Review review = ReviewMapper.mapToReview(request);
        review.setUseful(0);
        review = reviewJdbcStorage.addReview(review);
        eventRepository.addEvent(request.getUserId(), "REVIEW", "ADD", review.getReviewId());
        log.info("Review successfully created with id: {}", review.getReviewId());
        return ReviewMapper.mapToReviewDto(review);
    }

    @Override
    public ReviewDto updateReview(UpdateReviewRequest request) {
        log.info("Updating review with id: {}", request.getReviewId());
        Review updatedReview = reviewJdbcStorage.findById(request.getReviewId())
                .map(review -> {
                    log.debug("Updating review fields: {}", review);
                    Long userId = review.getUserId();
                    Review updated = ReviewMapper.updateReviewFields(review, request);
                    updated.setUserId(userId);
                    return updated;
                })
                .orElseThrow(() -> {
                    log.warn("Review with id {} not found for update", request.getReviewId());
                    return new NotFoundException("Not found for update review with id {}: " + request.getReviewId());
                });
        updatedReview = reviewJdbcStorage.updateReview(updatedReview);
        eventRepository.addEvent(updatedReview.getUserId(), "REVIEW", "UPDATE", updatedReview.getReviewId());
        log.info("Review with id {} updated successfully", updatedReview.getReviewId());
        return ReviewMapper.mapToReviewDto(updatedReview);
    }

    @Override
    public void deleteReviewById(Long id) {
        Long userId = reviewJdbcStorage.findById(id).orElseThrow(() ->
                new NotFoundException("Review with id " + id + " not found")).getUserId();
        boolean deleted = reviewJdbcStorage.deleteReview(id);
        if (!deleted) {
            throw new NotFoundException("Review with id " + id + " not found");
        }
        eventRepository.addEvent(userId, "REVIEW", "REMOVE", id);
        log.info("Review with id {} deleted successfully", id);
    }

    @Override
    public ReviewDto getReviewById(Long id) {
        Review review = reviewJdbcStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Review with id {} not found", id);
                    return new NotFoundException("Not found review with id {}: " + id);
                });
        return ReviewMapper.mapToReviewDto(review);
    }

    @Override
    public List<ReviewDto> getReviewsByFilmId(Long filmId, Integer count) {
        log.info("Getting reviews by film id {}", filmId);
        List<Review> reviews;
        reviews = reviewJdbcStorage.findReviews(filmId, count);
        log.info("Total reviews found {}", reviews.size());
        return reviews.stream()
                .map(ReviewMapper::mapToReviewDto)
                .toList();
    }

    @Override
    public void addUserLike(Long id, Long userId) {
        log.info("Add user like");
        reviewJdbcStorage.addLikeDislike(id, userId, true);
    }

    @Override
    public void addUserDislike(Long id, Long userId) {
        log.info("Add user dislike");
        reviewJdbcStorage.addLikeDislike(id, userId, false);
    }

    @Override
    public void deleteUserLike(Long id, Long userId) {
        log.info("Delete user like");
        reviewJdbcStorage.removeLikeDislike(id, userId, true);
    }

    @Override
    public void deleteUserDislike(Long id, Long userId) {
        log.info("Delete user dislike");
        reviewJdbcStorage.removeLikeDislike(id, userId, false);
    }


}
