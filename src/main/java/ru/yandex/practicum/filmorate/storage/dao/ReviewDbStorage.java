package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component("ReviewDbStorage")
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private static final String SQL_INSERT_REVIEW = "INSERT INTO reviews VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_REVIEW = "UPDATE reviews SET content = ?, is_positive = ? WHERE id_review = ?";
//    private static final String SQL_UPDATE_REVIEW = "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, " +
//            "film_id = ?, useful = ? WHERE review_id = ?";
    private static final String SQL_UPDATE_USEFUL = "UPDATE reviews SET useful = useful + ? WHERE id_review = ?";
    private static final String SQL_DELETE_REVIEW = "DELETE FROM reviews WHERE id_review = ?";
    private static final String SQL_GET_REVIEW_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String SQL_GET_REVIEWS_FOR_FILM = "SELECT * FROM reviews WHERE film_id = ? LIMIT ?";
    private static final String SQL_GET_ALL_REVIEW = "SELECT * FROM reviews";
    private static final String SQL_UPDATE_REVIEW_RATING = "UPDATE like_review SET rating = ? WHERE user_id = ? " +
            "AND id_review = ?";
    private static final String SQL_DELETE_MARK = "DELETE FROM like_review WHERE user_id = ? AND id_review = ?";
    private static final String SQL_INSERT_MARK = "INSERT INTO like_review (id_review, user_id, rating) " +
            "VALUES (?, ?, ?)";
    private static final String SQL_GET_REVIEW_RATING = "SELECT * FROM like_review WHERE user_id = ? AND id_review = ?";

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate, @Qualifier("UserDbStorage") UserStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
    }

    @Override
    public Review create(Review review) { // создать отзыв
        if (review.getReviewId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(SQL_INSERT_REVIEW, new String[]{"review_id"});
                stmt.setString(1, review.getContent());
                stmt.setBoolean(2, review.getIsPositive());
                stmt.setLong(3, review.getUserId());
                stmt.setLong(4, review.getFilmId());
                stmt.setInt(5, review.getUseful());
                return stmt;
            }, keyHolder);
            Objects.requireNonNull(keyHolder.getKey()).intValue();
            log.info("Добавлен отзыв на фильм с идентификатором: {}", review.getReviewId());
            return review;
        } else {
            jdbcTemplate.update(SQL_UPDATE_REVIEW,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getUserId(),
                    review.getFilmId(),
                    review.getUseful(),
                    review.getReviewId());
        }
        return review;
    }


    @Override
    public Review update(Review review) {
//        int queryResult = jdbcTemplate.update(SQL_UPDATE_REVIEW,
//                review.getContent(),
//                review.getIsPositive(),
//                review.getUserId(),
//                review.getFilmId(),
//                review.getUseful(),
//                review.getReviewId());
//        if (queryResult == 0) {
//            throw new NotFoundException("Не найдено ревью с идентификстором № " + review.getReviewId());
//        }

        jdbcTemplate.update(SQL_UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        log.info("Обновлен отзыв на фильм с идентификатором № {}.", review.getReviewId());
        return review;
    }

    @Override
    public void deleteById(int reviewId) {
        if (reviewId == 0) {
            log.debug("Отзыв на фильм с идентификатором {} для удаления не найден.", reviewId);
            throw new NotFoundException("Не найден отзыв на фильм с идентификатором № " + reviewId);
        } else {
            jdbcTemplate.update(SQL_DELETE_REVIEW, reviewId);
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
    public List<Review> getAllReviews() {
        return jdbcTemplate.query(SQL_GET_ALL_REVIEW, (this::mapRowToReview)).stream()
                .sorted((o1, o2) -> {
                    int result = Integer.compare(o1.getUseful(), o2.getUseful());
                    return result * -1;
                })
                .collect(Collectors.toList());
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
    public void addMarkReview(int reviewId, Long userId, int value) { // добавить отметку нравится отзыву
        if (getById(reviewId) == null || userStorage.getUserById(userId) == null) {
            throw new NotFoundException("При создании отметки нравится отзыву на фильм переданы " +
                    "некорректные данные пользователе или отзыве.");
        } else {
            SqlRowSet rowSet = jdbcTemplate.queryForRowSet(SQL_GET_REVIEW_RATING, userId, reviewId);
            if (rowSet.next()) {
                jdbcTemplate.update(SQL_UPDATE_REVIEW_RATING, value, userId, reviewId);
            } else {
                jdbcTemplate.update(SQL_INSERT_MARK, reviewId, userId, value);
            }
            updateUseful(reviewId, value);
        }
    }

    @Override
    public void deleteMarkReview(int reviewId, Long userId, int value) { // удалить отметку нравится отзыву
        if (getById(reviewId) == null || userStorage.getUserById(userId) == null) {
            throw new NotFoundException("При создании отметки нравится отзыву на фильм переданы " +
                    "некорректные данные пользователе или отзыве.");
        } else {
            updateUseful(reviewId, value);
            jdbcTemplate.update(SQL_DELETE_MARK, userId, reviewId);
        }
    }

    public void updateUseful(int reviewId, int i) {
        jdbcTemplate.update(SQL_UPDATE_USEFUL, i, reviewId);
    }

    private Review mapRowToReview(ResultSet resultSet, int i) throws SQLException {
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
