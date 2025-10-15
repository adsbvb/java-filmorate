package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ImplReviewService implements ReviewService{
    @Override
    public ReviewDto addReview(NewReviewRequest request) {
        return null;
    }

    @Override
    public ReviewDto updateReview(UpdateReviewRequest request) {
        return null;
    }

    @Override
    public void deleteReview(Long id) {

    }

    @Override
    public Optional<ReviewDto> getReviewById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<ReviewDto> getReviews(Long filmId, Integer count) {
        return List.of();
    }

    @Override
    public void likeReview(Long id, Long userId) {

    }

    @Override
    public void dislikeReview(Long id, Long userId) {

    }

    @Override
    public void removeLike(Long id, Long userId) {

    }

    @Override
    public void removeDislike(Long id, Long userId) {

    }
}
