package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.helpers.connector.ConnectToDB;

import java.sql.SQLException;

@Data
@RequiredArgsConstructor
@Repository
public class LikeStatusDbStorage {

    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private final ConnectToDB connectToDB;

    public void addLike(long filmId, long userId) throws SQLException {
        String query = "insert into like_status (film_id, user_id) values ('" + filmId + "', '" + userId + "')";
        connectToDB.getStatement().executeUpdate(query);

    }

    public void deleteLike(long filmId, long userId) {
        jdbcTemplate.update("delete from like_status where film_id = ? and user_id = ? "
                , filmId, userId);
    }

    public void deleteLikeByFilmId(long filmId) {
        jdbcTemplate.update("delete from like_status where film_id = ?", filmId);
    }

    public boolean likeWasDetected(long filmId, long userId) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from like_status where film_id = ? " +
                "and user_id = ?", filmId, userId);
        return sqlRowSet.first();
    }

    public long getAmountOfLikesOfFilmByFilmId(long filmId) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from like_status where film_id = ? ", filmId);
        sqlRowSet.last();
        return sqlRowSet.getRow();
    }

    public void deleteLikeByUserId(long userId) {
        jdbcTemplate.update("delete from like_status where user_id = ?", userId);
    }
}
