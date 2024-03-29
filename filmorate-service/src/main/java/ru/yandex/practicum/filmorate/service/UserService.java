package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.UserFriendsDbStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private final FilmService filmService;
    @Autowired
    private final UserDbStorage userStorage;
    @Autowired
    private final UserFriendsDbStorage userFriendsDbStorage;
    @Autowired
    private final EventDbStorage eventDbStorage;

    // Метод добавляющий пользователя в друзья
    public void addFriend(long userId, long friendId) throws SQLException {
        if (userId == friendId) {
            log.debug("Пользователь не может быть другом самому себе");
            throw new ValidationException("Ошибка валидации");
        }
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
                userFriendsDbStorage.addFriend(friendId, userId, false);
                log.debug("Обновляем статус дружбы");
                log.debug("Проверяем взаимность дружбы");
                if (userFriendsDbStorage.checkMutualFriendShip(friendId, userId)) {
                    userFriendsDbStorage.updateStatusOfFriendShip(friendId, userId);
                }
                log.debug("Записываем добавление друга в таблицы событий");
                eventDbStorage.addEvent(userId, friendId, "FRIEND", "ADD");
            } catch (RuntimeException e) {
                log.debug("Непредвиденная ошибка на сервере при добавлении друга");
                throw new RuntimeException("Внутреняя ошибка сервера");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Метод для удаления пользователя из друзей
    public void deleteFromFriends(Long userId, Long friendId) throws SQLException {
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
                userFriendsDbStorage.deleteRowOfFriendShip(friendId, userId);
                log.debug("Записываем удаление друга в таблицы событий");
                eventDbStorage.addEvent(userId, friendId, "FRIEND", "REMOVE");
            } catch (RuntimeException e) {
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    // Метод возвращающий список общих друзей
    public List<User> allCoincideFriends(Long userId, Long friendId) throws SQLException {
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
                if (friendsOfFriend.contains(us)) {
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
    public List<User> allFriendsOfUser(Long userId) throws SQLException {
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
                return userFriendsDbStorage.getListOfFriendsOfUser(userId);
            } catch (RuntimeException e) {
                log.debug("Пр попытке вернуть список друзей пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутренняя ошибка сервера");
            }
        }
    }

    // метод возвращает список рекомендуемых фильмов для пользователя,
    // основан на поиске пользователя с аналогичными лайками фильмов
    public List<Film> getRecommendations(Long userId) throws SQLException {
        if (userId < 0) {
            log.debug("При получении списка рекомендуемых фильмов возникла ошибка с ID пользователя={}", userId);
            throw new NotFoundException("Пользователь не найден");
        }
        if (userStorage.getUserById(userId) == null) {
            log.debug("При получении списка рекомендуемых фильмов пользователю возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Возвращаем список рекомендуемых фильмов для пользователя с ID={}", userId);
                return filmService.getRecommendations(userId);
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть рекомендуемые фильмы возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутренняя ошибка сервера");
            }
        }
    }
}
