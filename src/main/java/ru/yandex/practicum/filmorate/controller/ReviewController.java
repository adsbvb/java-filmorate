package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
@Validated
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto addReview(
            @Valid @RequestBody NewReviewRequest request
    ) {
        log.info("Request create new review");
        return reviewService.addReview(request);
    }

    @PutMapping
    public ReviewDto updateReview(
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        log.info("Request update review");
        return reviewService.updateReview(request);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(
            @PathVariable("id") @Positive Long id
    ) {
        log.info("Request deleteReview review");
        reviewService.deleteReviewById(id);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ReviewDto getReviewById(
            @PathVariable("id") @Positive Long id
    ) {
        log.info("Request get review by id");
        return reviewService.getReviewById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReviewDto> getReviews(
            @RequestParam(value = "filmId", required = false) Long filmId,
            @RequestParam(value = "count", required = false, defaultValue = "10") @Positive Integer count
    ) {
        log.info("Request get reviews by film_id");
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeReview(
            @PathVariable("id") @Positive Long id,
            @PathVariable("userId") @Positive Long userId
    ) {
        log.info("Request like review");
        reviewService.addUserLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeReview(
            @PathVariable("id") @Positive Long id,
            @PathVariable("userId") @Positive Long userId
    ) {
        log.info("Request dislike review");
        reviewService.addUserDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(
            @PathVariable("id") @Positive Long id,
            @PathVariable("userId") @Positive Long userId
    ) {
        log.info("Request deleteReview like review");
        reviewService.deleteUserLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(
            @PathVariable("id") @Positive Long id,
            @PathVariable("userId") @Positive Long userId
    ) {
        log.info("Request deleteReview dislike review");
        reviewService.deleteUserDislike(id, userId);
    }
}
