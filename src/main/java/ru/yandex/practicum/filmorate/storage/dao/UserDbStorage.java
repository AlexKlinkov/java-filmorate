package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.*;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component("UserDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserFriendsDbStorage userFriendsDbStorage;

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserFriendsDbStorage userFriendsDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userFriendsDbStorage = userFriendsDbStorage;
    }

    @Override
    public User create(User user) throws RuntimeException {
        if (user == null) {
            log.debug("При попытке создать нового пользователя произошла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        }
        log.debug("При создании пользователя проверяем, что данного пользователя еще нет в БД");
        SqlRowSet alreadyExist = jdbcTemplate.queryForRowSet("select * from USER_FILMORATE where EMAIL = ? ",
                user.getEmail());
        if (alreadyExist.first()) {
            log.debug("Если пользователь уже есть в БД, то не создаем его, а возвращаем из БД, " +
                    "обеспечивая уникальность данных");
            return getUserById(alreadyExist.getLong("ID"));
        }
        try {
            if (user.getName().isEmpty()) {
                user.setName(user.getLogin());
            }
            log.debug("Добавляем в базу нового пользователя");
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("USER_FILMORATE").usingGeneratedKeyColumns("ID");
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("EMAIL", user.getEmail())
                    .addValue("LOGIN", user.getLogin())
                    .addValue("NAME", user.getName())
                    .addValue("BIRTHDAY", Date.valueOf(user.getBirthday()));
            Number num = jdbcInsert.executeAndReturnKey(parameters);
            user.setId(num.intValue());
            return user;
        } catch (RuntimeException e) {
            log.debug("При попытке создать нового пользователя произошла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    @Override
    public User update(User user) throws RuntimeException {
        if (user == null) {
            log.debug("При обновлении пользователя передали значение Null");
            throw new ValidationException("Ошибка валидации");
        }
        log.debug("Обновляем пользователя в базе данных");
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from USER_FILMORATE where id = ?",
                user.getId());
        if (!sqlRowSet.first()) {
            log.debug("При обновлении пользователя объект с ID - " + user.getId() + " не был найден");
            throw new NotFoundException("Искомый объект не найден");
        } else {
            try {
                String sqlQuery = "UPDATE USER_FILMORATE SET " +
                        "EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? " + "where ID = ?";
                jdbcTemplate.update(sqlQuery,
                        user.getEmail(), user.getLogin(), user.getName(),
                        Date.valueOf(user.getBirthday()), user.getId());
                return user;
            } catch (RuntimeException e) {
                log.debug("При обновлении пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    @Override
    public void delete(User user) throws RuntimeException {
        if (user == null) {
            log.debug("При удалении пользователя возникла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from USER_FILMORATE where ID = ?",
                user.getId());
        if (!sqlRowSet.first()) {
            log.debug("При удалении пользователя возникла ошибка с ID");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Удалили пользователя");
                jdbcTemplate.update("delete from USER_FILMORATE where ID = ?", user.getId());
            } catch (RuntimeException e) {
                log.debug("При удалении пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    @Override
    public void deleteById(long id) {
        if (id <= 0) {
            log.debug("При попытке удалить пользователя возникла ошибка с ID: {}", id);
            throw new NotFoundException("Искомый объект не может быть найден");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from USER_FILMORATE where ID = ?", id);
        if (!sqlRowSet.first()) {
            log.debug("При удалении пользоваьедя возникла ошибка с ID: {}", id);
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                jdbcTemplate.update("delete from USER_FILMORATE where ID = ?", id);
                log.debug("Удалили пользователя");
            } catch (RuntimeException e) {
                log.debug("При удалении пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    @Override
    public List<User> getUsers() throws RuntimeException {
        try {
            log.debug("Возвращаем список со всеми пользователями");
            String sqlQuery = "select ID, EMAIL, LOGIN, NAME, BIRTHDAY from USER_FILMORATE";
            return jdbcTemplate.query(sqlQuery, this::makeUser);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми пользователями возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    @Override
    public User getUserById(long id) throws RuntimeException {
        if (id < 0) {
            log.debug("При попытке вернуть пользователя возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from USER_FILMORATE where ID = ?", id);
        if (!sqlRowSet.first()) {
            log.debug("При получения пользователя возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Возвращаем пользователя по ID");
                User user = new User(
                        sqlRowSet.getLong("ID"),
                        sqlRowSet.getString("EMAIL"),
                        sqlRowSet.getString("LOGIN"),
                        sqlRowSet.getString("NAME"),
                        Objects.requireNonNull(sqlRowSet.getDate("BIRTHDAY")).toLocalDate()
                );
                return user;
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
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
