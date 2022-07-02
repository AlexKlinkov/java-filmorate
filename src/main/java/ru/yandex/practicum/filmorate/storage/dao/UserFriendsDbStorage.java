package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
@Slf4j
@Component
public class UserFriendsDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserFriendsDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriend (long friendId, long userId, boolean status) {
        jdbcTemplate.update("merge into USER_FRIENDS (friend_id, user_filmorate_id, status) " +
                "values (?, ?, ?)", friendId, userId, status);
    }

    public boolean checkMutualFriendShip (long friendId, long userId) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from USER_FRIENDS where (FRIEND_ID = ? " +
                        "AND USER_FILMORATE_ID = ?) AND (USER_FILMORATE_ID = ? AND FRIEND_ID = ?)",
                friendId, userId, userId, friendId);
        sqlRowSet.last();
        return sqlRowSet.getRow() == 2;
    }

    public void updateStatusOfFriendShip (long friendId, long userId) {
        jdbcTemplate.queryForRowSet("update USER_FRIENDS set STATUS = true where (FRIEND_ID = ? " +
                        "and USER_FILMORATE_ID = ?) and (USER_FILMORATE_ID = ? and FRIEND_ID = ?)",
                friendId, userId, userId, friendId);
    }

    public void deleteRowOfFriendShip (long friendId, long userId) {
        jdbcTemplate.update("delete from USER_FRIENDS where FRIEND_ID = ? " +
                "and USER_FILMORATE_ID = ?", friendId, userId);
    }

    public List<User> getListOfFriendsOfUser (long userId) {
        String sql = "select USER_FILMORATE.ID," +
                "USER_FILMORATE.EMAIL," +
                "USER_FILMORATE.LOGIN," +
                "USER_FILMORATE.NAME," +
                "USER_FILMORATE.BIRTHDAY " +
                "from USER_FILMORATE left join USER_FRIENDS ON " +
                "USER_FILMORATE.ID = USER_FRIENDS.USER_FILMORATE_ID " +
                "where USER_FILMORATE.ID IN (" +
                "select FRIEND_ID " +
                "from USER_FRIENDS " +
                "where USER_FILMORATE_ID = " + userId + ")";
        return jdbcTemplate.query(sql, this :: makeUser);
    }
    private User makeUser(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Собираем объект в методе makeUser");
        return new User(
                resultSet.getLong("ID"),
                resultSet.getString("EMAIL"),
                resultSet.getString("LOGIN"),
                resultSet.getString("NAME"),
                resultSet.getDate("BIRTHDAY").toLocalDate());
    }
}
