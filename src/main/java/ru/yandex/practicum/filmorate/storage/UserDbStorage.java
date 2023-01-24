package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.helpers.connector.ConnectToDB;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Data
@RequiredArgsConstructor
@Repository
public class UserDbStorage {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private final ConnectToDB connectToDB;
    @Autowired
    private final UserFriendsDbStorage userFriendsDbStorage;
    @Autowired
    private final EventOfUserDbStorage eventOfUserDbStorage;
    @Autowired
    private final LikeStatusDbStorage likeStatusDbStorage;

    public User create(User user) throws RuntimeException, SQLException {
        if (user == null) {
            log.debug("При попытке создать нового пользователя произошла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        }
        log.debug("При создании пользователя проверяем, что данного пользователя еще нет в БД");
        SqlRowSet alreadyExist = jdbcTemplate.queryForRowSet("select * from user_filmorate where email = ? ",
                user.getEmail());
        if (alreadyExist.getRow() > 0) {
            log.debug("Если пользователь уже есть в БД, то не создаем его, а возвращаем из БД, " +
                    "обеспечивая уникальность данных");
            return getUserById(alreadyExist.getLong("id"));
        }
        try {
            if (user.getName().isEmpty()) {
                user.setName(user.getLogin());
            }
            log.debug("Добавляем в базу нового пользователя");
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("user_filmorate").usingGeneratedKeyColumns("id");
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("email", user.getEmail())
                    .addValue("login", user.getLogin())
                    .addValue("name", user.getName())
                    .addValue("birthday", user.getBirthday());
            Number num = jdbcInsert.executeAndReturnKey(parameters);
            user.setId(num.intValue());
            return user;
        } catch (RuntimeException e) {
            log.debug("При попытке создать нового пользователя произошла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    public User update(User user) throws RuntimeException, SQLException {
        if (user == null) {
            log.debug("При обновлении пользователя передали значение Null");
            throw new ValidationException("Ошибка валидации");
        }
        log.debug("Обновляем пользователя в базе данных");
        User userFromDB = getUserById(user.getId());
        if (userFromDB == null) {
            log.debug("При обновлении пользователя объект с ID - " + user.getId() + " не был найден");
            throw new NotFoundException("Искомый объект не найден");
        } else {
            try {
                String sqlQuery = "update user_filmorate set " +
                        "name = '" + user.getName() + "', login = '" + user.getLogin() + "', email = '" +
                        user.getEmail() + "', birthday = '" + user.getBirthday() + "' where id = " + user.getId();
                connectToDB.getStatement().executeUpdate(sqlQuery);
                return getUserById(user.getId());
            } catch (RuntimeException e) {
                log.debug("При обновлении пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    public void delete(User user) throws RuntimeException {
        if (user == null) {
            log.debug("При удалении пользователя возникла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from user_filmorate where id = ?",
                user.getId());
        if (!sqlRowSet.first()) {
            log.debug("При удалении пользователя возникла ошибка с ID");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Удалили пользователя");
                jdbcTemplate.update("delete from user_filmorate where id = ?", user.getId());
            } catch (RuntimeException e) {
                log.debug("При удалении пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    public void deleteById(Long id) {
        if (id <= 0) {
            log.debug("При попытке удалить пользователя возникла ошибка с ID: {}", id);
            throw new NotFoundException("Искомый объект не может быть найден");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from user_filmorate where id = ?", id);
        if (!sqlRowSet.first()) {
            log.debug("При удалении пользователя возникла ошибка с ID: {}", id);
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                eventOfUserDbStorage.deleteRecordFromTableEventOfUserByUserId(id);
                likeStatusDbStorage.deleteLikeByUserId(id);
                userFriendsDbStorage.deleteRowByUserId(id);
                jdbcTemplate.update("delete from user_filmorate where id = ?", id);
                log.debug("Удалили пользователя");
            } catch (RuntimeException | SQLException e) {
                log.debug("При удалении пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    public List<User> getUsers() throws RuntimeException {
        try {
            log.debug("Возвращаем список со всеми пользователями");
            String sqlQuery = "select id, email, login, name, birthday from user_filmorate";
            return jdbcTemplate.query(sqlQuery, this::makeUser);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми пользователями возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    public User getUserById(long id) throws RuntimeException, SQLException {
        if (id < 0) {
            log.debug("При попытке вернуть пользователя возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from user_filmorate where id = ?", id);
        if (!userRows.first()) {
            log.debug("При получения пользователя возникла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        } else {
            try {
                log.debug("Возвращаем пользователя по ID - " + id);
                return getUsers().stream()
                        .filter((x) -> x.getId() == id)
                        .collect(Collectors.toList()).get(0);
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    private User makeUser(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Собираем объект в методе makeUser");
        User user = new User(
                resultSet.getLong("id"),
                resultSet.getString("email"),
                resultSet.getString("login"),
                resultSet.getString("name"),
                resultSet.getDate("birthday").toLocalDate());
        return user;
    }
}
