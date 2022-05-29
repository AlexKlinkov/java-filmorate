package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    // Внедряем доступ сервиса к хранилищу
    @Autowired
    public UserService(@Qualifier("InMemoryUserStorage") UserStorage userStorage)  {
        this.userStorage = userStorage;
    }

    // Метод добавляющий пользователя в друзья
    public void addFriend(long id, long friendId) {
        System.out.println(friendId);
        if (id < 0 || friendId < 0) {
            log.debug("Друг не добавился, ошибка с ID (пользователя или друга");
            throw new NotFoundException("Искомый объект не найден");
        }
        log.debug("Получаем друга из хранилища по ID - " + friendId);
        User friend = userStorage.getUserById(friendId);
        log.debug("Из хранилища получаю пользователя по ID - " + id);
        User user = userStorage.getUserById(id);
        if (friend == null || user == null) {
            log.debug("Друг не добавился, ошибка с ID (пользователя или друга");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Добавляем пользователю нового друга");
                user.getFriendsID().add(friendId);
                log.debug("Теперь другу добавляем пользователя, в качестве тоже друга");
                friend.getFriendsID().add(id);
                System.out.println(friend);
                System.out.println(user);
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
        } else if (userStorage.getUserById(userId) == null || userStorage.getUserById(friendId) == null) {
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Из хранилища получаю пользователя по ID");
                User user = userStorage.getUserById(userId);
                log.debug("Удаляем друга из друзей пользователя (его ID)");
                user.getFriendsID().remove(friendId);
                log.debug("Обновляем информацию хранящуюся в хранилище");
                userStorage.getUsers().add(user);
                log.debug("Получаем друга из хранилища");
                User friend = userStorage.getUserById(friendId);
                log.debug("У друга удаляем пользователя из друзей");
                friend.getFriendsID().remove(userId);
                log.debug("Обновляем информацию хранящуюся в хранилище");
                userStorage.getUsers().add(friend);
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
            List<User> allFriendsWhichCoincide = new ArrayList<>();
            log.debug("Из хранилища получаю пользователя по ID");
            User user = userStorage.getUserById(userId);
            log.debug("Получаем друга из хранилища");
            User friend = userStorage.getUserById(friendId);
            log.debug("Проходимся циклом по всем ID друзей пользователя");
            for (Long cycleUser : user.getFriendsID()) {
                log.debug("Проверяем совпадают ли ID друзей пользователя и ID друзей друга");
                if (friend.getFriendsID().contains(cycleUser)) {
                    log.debug("Добавляем пользователя в список с общими друзьями");
                    allFriendsWhichCoincide.add(userStorage.getUserById(cycleUser));
                }
            }
            return allFriendsWhichCoincide;
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
                List<User> allFriends = new ArrayList<>();
                log.debug("Из хранилища получаю пользователя при возвращении списка друзей пользователя");
                User user = userStorage.getUserById(userId);
                // Прохожусь циклом по множеству (id друзей) и из хранилища выдергиваю их и добавляю
                // в возвращаемый список
                for (Long id : user.getFriendsID()) {
                    allFriends.add(userStorage.getUserById(id));
                }
                log.debug("Пытаемся вернуть список со всеми друзьями пользователя");
                return allFriends;
            } catch (RuntimeException e) {
                log.debug("Пр попытке вернуть список друзей пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутренняя ошибка сервера");
            }
        }
    }
}
