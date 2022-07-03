package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
public class ReviewsService { // добавление / редактирование / удаление отзывов на фильмы
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public ReviewsService(@Qualifier("ReviewDbStorage") ReviewStorage reviewStorage,
                          @Qualifier("UserDbStorage") UserStorage userStorage,
                          @Qualifier("FilmDbStorage") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    // Создание отзыва на фильм
    public Review create(Review review) {
        validate(review);
        return reviewStorage.create(review);
    }

    // Редактирование уже имеющегося отзыва на фильм
    public Review update(Review review) {
        validate(review);
        return reviewStorage.update(review);
    }

    // Удаление уже имеющегося отзыва по идентификатору отзыва
    public void deleteById(int reviewId) {
        reviewStorage.deleteById(reviewId);
    }

    // Получение отзыва по идентификатору отзыва
    public Review getById(int reviewId) {
        return reviewStorage.getById(reviewId);
    }

    // Получение всех отзывов по идентификатору фильма
    public List<Review> getReviewsForFilm(Long filmId, int count) {
        return reviewStorage.getReviewsForFilm(filmId, count);
    }

    // Пользователь ставит лайк отзыву на фильм
    public void addLikeReview(Integer reviewId, Long userId) {
        validate(getById(reviewId));
        Integer useful = reviewStorage.getById(reviewId).getUseful();
        reviewStorage.updateLike(++useful, reviewId);
    }

    // Пользователь ставит дизлайк отзыву на фильм
    public void addDislikeReview(Integer reviewId, Long userId) {
        validate(getById(reviewId));
        Integer useful = reviewStorage.getById(reviewId).getUseful();
        reviewStorage.updateLike(--useful, reviewId);
    }

    // Пользователь удаляет лайк отзыву на фильм
    public void deleteLikeFromReview(int reviewId, Long userId) {
        addDislikeReview(reviewId, userId);
    }

    // Пользователь удаляет дизлайк отзыву на фильм
    public void deleteDislikeFromReview(int reviewId, Long userId) {
        addLikeReview(reviewId, userId);
    }

    private void validate(Review review) {
        if (review.getFilmId() != null) {
            if (filmStorage.getFilmById(review.getFilmId()) == null) {
                log.debug("Для действий с отзывом передан несуществующий фильм {}.", review.getFilmId());
                throw new ValidationException("Для действий с отзывом передан несуществующий фильм.");
            }
        } else {
            throw new ValidationException("Для действий с отзывом передан отзыв с пустым ID фильма.");
        }
        if (review.getUserId() != null) {
            if (userStorage.getUserById(review.getUserId()) == null) {
                log.debug("Для действий с отзывом передан несуществующий пользователь {}.", review.getUserId());
                throw new ValidationException("Для действий с отзывом передан несуществующий пользователь.");
            }
        } else {
            throw new ValidationException("Для действий с отзывом передан отзыв с пустым ID пользователя.");
        }
        if (review.getIsPositive() == null) {
            log.debug("Для действий с отзывом поле isPositive не заполнено.");
            throw new ValidationException("Для действий с отзывом не указан тип отзыва: позитивный или негативный.");
        }
    }
}
