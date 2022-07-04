package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("ReviewDbStorage")
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private static final String SQL_UPDATE_REVIEW = "UPDATE reviews SET content = ?, is_positive = ? " +
            "WHERE review_id = ?";
    private static final String SQL_UPDATE_USEFUL = "UPDATE reviews SET useful = ? WHERE review_id = ?";
    private static final String SQL_DELETE_REVIEW = "DELETE FROM reviews WHERE review_id = ?";
    private static final String SQL_GET_REVIEW_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String SQL_GET_REVIEWS_FOR_FILM = "SELECT * FROM reviews WHERE film_id = ? LIMIT ?";
    private static final String SQL_GET_ALL_REVIEW = "SELECT * FROM reviews";

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate, @Qualifier("UserDbStorage") UserStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
    }

    @Override
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

    @Override
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

    @Override
    public List<Review> getAllReviews() {
        return jdbcTemplate.query(SQL_GET_ALL_REVIEW, (this::mapRowToReview)).stream()
                .sorted((o1, o2) -> {
                    int result = Integer.compare(o1.getUseful(), o2.getUseful());
                    return result * -1;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(int reviewId) {
        if (reviewId == 0) {
            log.debug("Отзыв на фильм с идентификатором {} для удаления не найден.", reviewId);
            throw new NotFoundException("Не найден отзыв на фильм с идентификатором № " + reviewId);
        } else {
            jdbcTemplate.update(SQL_DELETE_REVIEW, reviewId);
            log.info("Удален отзыв с идентификатором {}.", reviewId);
        }
    }

    @Override
    public Review getById(int reviewId) {
        try {
            return jdbcTemplate.queryForObject(SQL_GET_REVIEW_BY_ID, this::mapRowToReview, reviewId);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Отзыв на фильм с идентификатором {} не найден.", reviewId);
            throw new NotFoundException("В Filmorate отсутствует отзыв на фильм с идентификатором № " + reviewId);
        }
    }

    @Override
    public List<Review> getReviewsForFilm(Long filmId, int count) {
        return jdbcTemplate.query(SQL_GET_REVIEWS_FOR_FILM, (this::mapRowToReview), filmId, count).stream()
                .sorted((o1, o2) -> {
                    int result = Integer.compare(o1.getUseful(), o2.getUseful());
                    return result * -1;
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
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
