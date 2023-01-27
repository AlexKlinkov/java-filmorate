package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.helpers.connector.ConnectToDB;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.util.List;

@Slf4j
@Data
@RequiredArgsConstructor
@Repository
public class UserFriendsDbStorage {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private ConnectToDB connectToDB;

    public void addFriend(long friendId, long userId, boolean status) throws SQLException {
        log.debug("Заполняем таблицу user_friends при добавлении друга");
        String firstVar = "select * from user_friends where friend_id =" + friendId +
                " and user_filmorate_id =" + userId;
        String secondVar = "select * from user_friends where friend_id =" + userId +
                " and user_filmorate_id =" + friendId;
        SqlRowSet first = jdbcTemplate.queryForRowSet(firstVar);
        SqlRowSet second = jdbcTemplate.queryForRowSet(secondVar);
        if (first.getRow() > 0 || second.getRow() > 0) {
            log.debug("Данная запись уже есть в таблице user_friends");
            return;
        }
        String query = "insert into user_friends (friend_id, user_filmorate_id, status) values " +
                "('" + friendId + "', '" + userId + "', '" + status + "')";
        connectToDB.getStatement().executeUpdate(query);
    }

    public boolean checkMutualFriendShip(long friendId, long userId) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from user_friends where (friend_id = ? " +
                        "and user_filmorate_id = ?) and (user_filmorate_id = ? and friend_id = ?)",
                friendId, userId, userId, friendId);
        sqlRowSet.last();
        return sqlRowSet.getRow() == 2;
    }

    public void updateStatusOfFriendShip(long friendId, long userId) {
        jdbcTemplate.queryForRowSet("update user_friends set status = true where (friend_id = ? " +
                        "and user_filmorate_id = ?) and (user_filmorate_id = ? and friend_id = ?)",
                friendId, userId, userId, friendId);
    }

    public void deleteRowOfFriendShip(long friendId, long userId) {
        jdbcTemplate.update("delete from user_friends where friend_id = ? " +
                "and user_filmorate_id = ?", friendId, userId);
    }

    public List<User> getListOfFriendsOfUser(long userId) {
        String sql = "select user_filmorate.id," +
                "user_filmorate.email," +
                "user_filmorate.login," +
                "user_filmorate.name," +
                "user_filmorate.birthday " +
                "from user_filmorate left join user_friends ON " +
                "user_filmorate.id = user_friends.user_filmorate_id " +
                "where user_filmorate.id IN (" +
                "select friend_id " +
                "from user_friends " +
                "where user_filmorate_id = " + userId + ")";
        return jdbcTemplate.query(sql, this::makeUser);
    }

    public void deleteRowByUserId(long userId) {
        jdbcTemplate.update("delete from user_friends where user_filmorate_id = ?", userId);
    }

    private User makeUser(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Собираем объект в методе makeUser");
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("email"),
                resultSet.getString("login"),
                resultSet.getString("name"),
                resultSet.getDate("birthday").toLocalDate());
    }
}
