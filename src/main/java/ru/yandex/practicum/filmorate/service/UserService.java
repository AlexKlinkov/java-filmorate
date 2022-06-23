package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage, JdbcTemplate jdbcTemplate)  {
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    // Метод добавляющий пользователя в друзья
    public void addFriend(long userId, long friendId) {
        System.out.println(friendId);
        if (userId < 0 || friendId < 0) {
            log.debug("Друг не добавился, ошибка с ID (пользователя или друга");
            throw new NotFoundException("Искомый объект не найден");
        }
        log.debug("Получаем друга из хранилища по ID - " + friendId);
        User friend = userStorage.getUserById(friendId);
        log.debug("Из хранилища получаю пользователя по ID - " + userId);
        User user = userStorage.getUserById(userId);
        if (friend == null || user == null) {
            log.debug("Друг не добавился, ошибка с ID (пользователя или друга");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Добавляем пользователю нового друга");
                jdbcTemplate.update("merge into USER_FRIENDS (friend_id, user_filmorate_id, status) " +
                        "values (?, ?, ?)", friend.getId(), user.getId(), false);
                log.debug("Обновляем статус дружбы");
                log.debug("Проверяем взаимность дружбы");
                SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from USER_FRIENDS where (FRIEND_ID = ? " +
                        "AND USER_FILMORATE_ID = ?) AND (USER_FILMORATE_ID = ? AND FRIEND_ID = ?)",
                        friend.getId(), user.getId(), user.getId(), friend.getId());
                sqlRowSet.last();
                if (sqlRowSet.getRow() == 2) {
                    jdbcTemplate.queryForRowSet("update USER_FRIENDS set STATUS = true where (FRIEND_ID = ? " +
                                    "and USER_FILMORATE_ID = ?) and (USER_FILMORATE_ID = ? and FRIEND_ID = ?)",
                            friend.getId(), user.getId(), user.getId(), friend.getId());
                }
            } catch (RuntimeException e) {
                log.debug("Непредвиденная ошибка на сервере при добавлении друга");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    // Метод для удаления пользователя из друзей
    public void deleteFromFriends(Long userId, Long friendId) {
        if (userId < 0 || friendId < 0) {
            throw new NotFoundException("Искомый объект не найден");
        }
        log.debug("Получаем друга из хранилища по ID - " + friendId);
        User friend = userStorage.getUserById(friendId);
        log.debug("Из хранилища получаю пользователя по ID - " + userId);
        User user = userStorage.getUserById(userId);
        if (friend == null || user == null) {
            log.debug("Друг не добавился, ошибка с ID (пользователя или друга");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Удаляем у пользователя друга");
                jdbcTemplate.update("delete from USER_FRIENDS where FRIEND_ID = ? " +
                                "and USER_FILMORATE_ID = ?", friendId, userId);
            } catch (RuntimeException e) {
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    // Метод возвращающий список общих друзей
    public List<User> allCoincideFriends(Long userId, Long friendId) {
        if (userId < 0 || friendId < 0) {
            log.debug("При попытке список общих друзей возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        if (userStorage.getUserById(userId) == null || userStorage.getUserById(friendId) == null) {
            log.debug("При попытке создать нового пользователя произошла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        }
        try {
            List<User> friendsOfUser = allFriendsOfUser(userId);
            List<User> friendsOfFriend = allFriendsOfUser(friendId);
            ArrayList<User> result = new ArrayList<>();
            for (User us : friendsOfUser) {
                if (friendsOfFriend.contains(us)){
                    result.add(us);
                }
            }
            return result;
        } catch (Throwable e) {
            log.debug("При возвращении списка с общими друзьями возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутренняя ошибка сервера");
        }
    }

    // Метод возвращающий список друзей пользователя
    public List<User> allFriendsOfUser(Long userId) {
        if (userId < 0) {
            log.debug("При получении списка всех друзей пользователя возникла ошибка с ID пользователя");
            throw new NotFoundException("Искомый объект не найден");
        }
        if (userStorage.getUserById(userId) == null) {
            log.debug("При получении списка всех друзей пользователя возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Возвращаем список с друзьями пользователя");
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
            } catch (RuntimeException e) {
                log.debug("Пр попытке вернуть список друзей пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутренняя ошибка сервера");
            }
        }
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
