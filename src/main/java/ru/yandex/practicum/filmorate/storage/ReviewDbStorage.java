package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Data
@RequiredArgsConstructor
@Repository
public class ReviewDbStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String SQL_UPDATE_REVIEW = "update reviews set content = ?, is_positive = ? " +
            "where review_id = ?";
    private static final String SQL_UPDATE_USEFUL = "update reviews set useful = ? where review_id = ?";
    private static final String SQL_DELETE_REVIEW = "delete from reviews where review_id = ?";
    private static final String SQL_GET_REVIEW_BY_ID = "select * from reviews where review_id = ?";
    private static final String SQL_GET_REVIEWS_FOR_FILM = "select * from reviews where film_id = ? limit ?";
    private static final String SQL_GET_ALL_REVIEW = "select * from reviews";

    public Review create(Review review) { // создать отзыв
        if (review.getReviewId() == null) {
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("reviews")
                    .usingGeneratedKeyColumns("review_id");
            review.setReviewId(simpleJdbcInsert.executeAndReturnKey(review.toMap()).intValue());
            log.info("Создан отзыв с идентификатором {}.", review.getReviewId());
            return review;
        } else {
            jdbcTemplate.update(SQL_UPDATE_REVIEW,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getUserId(),
                    review.getFilmId(),
                    review.getUseful(),
                    review.getReviewId());
            log.info("Обновлен отзыв на фильм с идентификатором № {}.", review.getReviewId());
        }
        return review;
    }

    public Review update(Review review) {
        if (review.getReviewId() == null) {
            throw new NotFoundException("Передан пустой ID отзыва");
        } else if (getById(review.getReviewId()) == null) {
            throw new NotFoundException("Не существует отзыва на фильм с идентификатором № " + review.getReviewId());
        }
        jdbcTemplate.update(SQL_UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        log.info("Обновлен отзыв на фильм с идентификатором № {}.", review.getReviewId());
        return review;
    }

    public List<Review> getAllReviews() {
        return jdbcTemplate.query(SQL_GET_ALL_REVIEW, (this::mapRowToReview)).stream()
                .sorted((o1, o2) -> {
                    int result = Integer.compare(o1.getUseful(), o2.getUseful());
                    return result * -1;
                })
                .collect(Collectors.toList());
    }

    public void deleteById(int reviewId) {
        if (reviewId == 0) {
            log.debug("Отзыв на фильм с идентификатором {} для удаления не найден.", reviewId);
            throw new NotFoundException("Не найден отзыв на фильм с идентификатором № " + reviewId);
        } else {
            jdbcTemplate.update(SQL_DELETE_REVIEW, reviewId);
            log.info("Удален отзыв с идентификатором {}.", reviewId);
        }
    }

    public Review getById(int reviewId) {
        try {
            return jdbcTemplate.queryForObject(SQL_GET_REVIEW_BY_ID, this::mapRowToReview, reviewId);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Отзыв на фильм с идентификатором {} не найден.", reviewId);
            throw new NotFoundException("В Filmorate отсутствует отзыв на фильм с идентификатором № " + reviewId);
        }
    }

    public List<Review> getReviewsForFilm(Long filmId, int count) {
        return jdbcTemplate.query(SQL_GET_REVIEWS_FOR_FILM, (this::mapRowToReview), filmId, count).stream()
                .sorted((o1, o2) -> {
                    int result = Integer.compare(o1.getUseful(), o2.getUseful());
                    return result * -1;
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    public void updateLike(int useful, Integer reviewId) {
        jdbcTemplate.update(SQL_UPDATE_USEFUL, useful, reviewId);
    }

    private Review mapRowToReview(ResultSet resultSet, int i) throws SQLException {
        if (resultSet.getRow() == 0) {
            throw new NotFoundException("Отзыв на фильм не найден в storage.");
        }
        return Review.builder()
                .reviewId(resultSet.getInt("review_id"))
                .content(resultSet.getString("content"))
                .isPositive(resultSet.getBoolean("is_positive"))
                .userId(resultSet.getLong("user_id"))
                .filmId(resultSet.getLong("film_id"))
                .useful(resultSet.getInt("useful"))
                .build();
    }
}
