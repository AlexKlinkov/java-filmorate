package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundExceptionFilmorate;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewsService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewsController {
    private final ReviewsService service;

    @Autowired
    public ReviewsController(ReviewsService service) {
        this.service = service;
    }

    // Добавление нового отзыва на фильм
    @PostMapping
    public Review create(@RequestBody Review review) throws NotFoundExceptionFilmorate {
        return service.create(review);
    }

    // Редактирование уже имеющегося отзыва на фильм
    @PutMapping
    public Review update(@RequestBody Review review) throws NotFoundExceptionFilmorate {
        return service.update(review);
    }

    // Удаление уже имеющегося отзыва по идентификатору отзыва
    @DeleteMapping("/{reviewId}")
    public void deleteById(@PathVariable int reviewId) throws NotFoundExceptionFilmorate {
        service.deleteById(reviewId);
    }

    // Получение отзыва по идентификатору отзыва
    @GetMapping("/{reviewId}")
    public Review getById(@PathVariable int reviewId) throws NotFoundExceptionFilmorate {
        return service.getById(reviewId);
    }

    @GetMapping
    public List<Review> getReviewsForFilm(
            @RequestParam(value = "filmId", defaultValue = "-1", required = false) Long filmId,
            @RequestParam(value = "count", defaultValue = "10", required = false) int count){
        if (filmId == -1) {
            return service.getAllReviews();
        }
        log.info("Получен список отзывов в количестве {} на фильм с идентификатором {} в контроллере.", count, filmId);
        return service.getReviewsForFilm(filmId, count);
    }

    // Пользователь ставит лайк отзыву на фильм
    @PutMapping("/{reviewId}/like/{userId}")
    public void addLikeReview(@PathVariable int reviewId, @PathVariable Long userId) throws NotFoundExceptionFilmorate {
        log.info("Добавлена отметка нравится от пользователя с идентификатором {} в контроллере." +
                "на отзыв на фильма с идентификатором {}", userId, reviewId);
        service.addLikeReview(reviewId, userId);
    }

    // Пользователь ставит дизлайк отзыву на фильм
    @PutMapping("/{reviewId}/dislike/{userId}")
    public void addDislikeReview(@PathVariable int reviewId, @PathVariable Long userId) throws NotFoundExceptionFilmorate {
        log.info("Добавлена отметка не нравится от пользователя с идентификатором {} в контроллере." +
                "на отзыв на фильма с идентификатором {}", userId, reviewId);
        service.addDislikeReview(reviewId, userId);
    }

    // Пользователь удаляет лайк отзыву на фильм
    @DeleteMapping("/{reviewId}/like/{userId}")
    public void removeLikeFromReview(@PathVariable int reviewId, @PathVariable Long userId) throws NotFoundExceptionFilmorate {
        log.info("Удалена отметка нравится от пользователя с идентификатором {} в контроллере." +
                "на отзыв на фильма с идентификатором {}", userId, reviewId);
        service.deleteLikeFromReview(reviewId, userId);
    }

    // Пользователь удаляет дизлайк отзыву на фильм
    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public void removeDislikeFromReview(@PathVariable int reviewId, @PathVariable Long userId) throws NotFoundExceptionFilmorate {
        log.info("Удалена отметка не нравится от пользователя с идентификатором {} в контроллере." +
                "на отзыв на фильма с идентификатором {}", userId, reviewId);
        service.deleteDislikeFromReview(reviewId, userId);
    }
}
