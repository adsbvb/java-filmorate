package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ReviewJdbcStorage extends BaseRepository<Review> implements ReviewRepository {
    public ReviewJdbcStorage(JdbcTemplate jdbcTemplate, RowMapper<Review> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Optional<Review> findById(Long id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            return findOne(sql, id);
        } catch (InternalServerException e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @Override
    public Review addReview(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                " VALUES (?, ?, ?, ?, ?)";
        try {
            Long reviewId = insert(
                    sql,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getUserId(),
                    review.getFilmId(),
                    review.getUseful()
            );
            review.setReviewId(reviewId);
            return review;
        } catch (InternalServerException e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ?, useful = ? WHERE review_id = ?";
        try {
            Long reviewId = review.getReviewId();
            update(
                    sql,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getUseful(),
                    reviewId
            );
            return review;
        } catch (InternalServerException e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @Override
    public boolean deleteReview(Long id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        try {
            int rows = jdbcTemplate.update(sql, id);
            return rows > 0;
        } catch (InternalServerException e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @Override
    public List<Review> findReviews(Long filmId, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM reviews");
        try {
            if (filmId != null) {
                sql.append(" WHERE film_id = ?");
            }
            sql.append(" ORDER BY review_id DESC LIMIT ?");
            if (filmId != null) {
                return jdbcTemplate.query(sql.toString(), new Object[]{filmId, limit}, mapper);
            } else {
                return jdbcTemplate.query(sql.toString(), new Object[]{limit}, mapper);
            }
        } catch (InternalServerException e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @Override
    public void addLikeDislike(Long reviewId, Long userId, boolean isLike) {
        String sql = "MERGE INTO review_likes_dislikes (review_id, user_id, is_like) KEY (review_id, user_id) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, reviewId, userId, isLike);
            refreshUseful(reviewId);
        } catch (InternalServerException e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @Override
    public void removeLikeDislike(Long reviewId, Long userId, boolean isLike) {
        String sql = "DELETE FROM review_likes_dislikes WHERE review_id = ? AND user_id = ? AND is_like = ?";
        try {
            jdbcTemplate.update(sql, reviewId, userId, isLike);
            refreshUseful(reviewId);
        } catch (InternalServerException e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    private void updateUseful(Long reviewId, int useful) {
        String sql = "UPDATE reviews SET useful = ? WHERE review_id = ?";
        try {
            jdbcTemplate.update(sql, useful, reviewId);
        } catch (InternalServerException e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    private void refreshUseful(Long reviewId) {
        String sql = "SELECT " +
                "SUM(CASE WHEN is_like THEN 1 ELSE 0 END) AS likes, " +
                "SUM(CASE WHEN is_like THEN 0 ELSE 1 END) AS dislikes " +
                "FROM review_likes_dislikes WHERE review_id = ?";
        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, reviewId);
            int likes = result.get("likes") != null ? ((Number) result.get("likes")).intValue() : 0;
            int dislikes = result.get("dislikes") != null ? ((Number) result.get("dislikes")).intValue() : 0;
            updateUseful(reviewId, likes - dislikes);
        } catch (EmptyResultDataAccessException e) {
            updateUseful(reviewId, 0);
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }
}