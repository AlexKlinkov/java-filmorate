package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundExceptionFilmorate;
import ru.yandex.practicum.filmorate.exception.ValidationExceptionFilmorate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserFriendsDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    private final UserFriendsDbStorage userFriendsDbStorage;
    private final EventDbStorage eventDbStorage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage, JdbcTemplate jdbcTemplate, UserFriendsDbStorage userFriendsDbStorage, EventDbStorage eventDbStorage) {
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
        this.userFriendsDbStorage = userFriendsDbStorage;
        this.eventDbStorage = eventDbStorage;
    }

    // Метод добавляющий пользователя в друзья
    public void addFriend(long userId, long friendId) {
        System.out.println(friendId);
        if (userId < 0 || friendId < 0) {
            log.debug("Друг не добавился, ошибка с ID (пользователя или друга");
            throw new NotFoundExceptionFilmorate("Искомый объект не найден");
        }
        log.debug("Получаем друга из хранилища по ID - " + friendId);
        User friend = userStorage.getUserById(friendId);
        log.debug("Из хранилища получаю пользователя по ID - " + userId);
        User user = userStorage.getUserById(userId);
        if (friend == null || user == null) {
            log.debug("Друг не добавился, ошибка с ID (пользователя или друга");
            throw new ValidationExceptionFilmorate("Ошибка валидации");
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
            }
        }
    }

    // Метод для удаления пользователя из друзей
    public void deleteFromFriends(Long userId, Long friendId) {
        if (userId < 0 || friendId < 0) {
            throw new NotFoundExceptionFilmorate("Искомый объект не найден");
        }
        log.debug("Получаем друга из хранилища по ID - " + friendId);
        User friend = userStorage.getUserById(friendId);
        log.debug("Из хранилища получаю пользователя по ID - " + userId);
        User user = userStorage.getUserById(userId);
        if (friend == null || user == null) {
            log.debug("Друг не добавился, ошибка с ID (пользователя или друга");
            throw new ValidationExceptionFilmorate("Ошибка валидации");
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
    public List<User> allCoincideFriends(Long userId, Long friendId) {
        if (userId < 0 || friendId < 0) {
            log.debug("При попытке список общих друзей возникла ошибка с ID");
            throw new NotFoundExceptionFilmorate("Искомый объект не найден");
        }
        if (userStorage.getUserById(userId) == null || userStorage.getUserById(friendId) == null) {
            log.debug("При попытке создать нового пользователя произошла ошибка с NULL");
            throw new NotFoundExceptionFilmorate("Искомый объект не найден");
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
    public List<User> allFriendsOfUser(Long userId) {
        if (userId < 0) {
            log.debug("При получении списка всех друзей пользователя возникла ошибка с ID пользователя");
            throw new NotFoundExceptionFilmorate("Искомый объект не найден");
        }
        if (userStorage.getUserById(userId) == null) {
            log.debug("При получении списка всех друзей пользователя возникла ошибка с NULL");
            throw new ValidationExceptionFilmorate("Ошибка валидации");
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
}
