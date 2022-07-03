package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    void deleteById(int reviewId);

    Review getById(int reviewId);
    List<Review> getReviewsForFilm(Long filmId, int count);

    List<Review> getAllReviews ();

    void addMarkReview(int reviewId, Long userId, int value);

    void deleteMarkReview(int reviewId, Long userId, int value);
}
