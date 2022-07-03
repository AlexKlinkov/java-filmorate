package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

@Component
public class LikeStatusDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public LikeStatusDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addLike (long filmId, long userId) {
        jdbcTemplate.update("MERGE INTO LIKE_STATUS (FILM_ID, USER_ID) " +
                "VALUES (?, ?)", filmId, userId);
    }

    public void deleteLike (long filmId, long userId) {
        jdbcTemplate.update("DELETE FROM LIKE_STATUS where FILM_ID = ? AND USER_ID = ? "
                , filmId, userId);
    }

    public void deleteLikeByFilmId (long filmId) {
        jdbcTemplate.update("delete from LIKE_STATUS where FILM_ID = ?", filmId);
    }

    public boolean likeWasDetected (long filmId, long userId) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from LIKE_STATUS where FILM_ID = ? " +
                "AND USER_ID = ?", filmId, userId);
        return sqlRowSet.first();
    }

    public long getAmountOfLikesOfFilmByFilmId (long filmId) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from LIKE_STATUS where FILM_ID = ? ", filmId);
        sqlRowSet.last();
        return sqlRowSet.getRow();
    }
}
