package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Getter
@Service
public class UserService {

    private final UserStorage userStorage;

    // Внедряем доступ сервиса к хранилищу
    @Autowired
    public UserService(@Qualifier("InMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    // Метод добавляющий пользователя в друзья
    public void addFriend(Long userId, Long friendId) throws ValidationException {
        try {
            // Из хранилища получаю пользователя по ID
            User user = userStorage.getAllUsers().get(userId);
            // Добавляем пользователю нового друга
            user.getFriendsID().add(friendId);
            // Получаем друга из хранилища
            User friend = userStorage.getAllUsers().get(friendId);
            // Теперь другу добавляем пользователя, в качестве тоже друга
            friend.getFriendsID().add(userId);
            // Обновляем информацию хранящуюся в хранилище
            userStorage.getAllUsers().put(userId, user);
            userStorage.getAllUsers().put(friendId, friend);
        } catch (Exception e) {
            throw new ValidationException("Пользователи не добавлены друг другу в друзья, проверьте ID");
        }
    }

    // Метод для удаления пользователя из друзей
    public void deleteFromFriends(Long userId, Long friendId) throws ValidationException {
        try {
            // Из хранилища получаю пользователя по ID
            User user = userStorage.getAllUsers().get(userId);
            // Удаляем друга из друзей пользователя (его ID)
            user.getFriendsID().remove(friendId);
            // Получаем друга из хранилища
            User friend = userStorage.getAllUsers().get(friendId);
            // У друга удаляем пользователя из друзей
            friend.getFriendsID().remove(userId);
            // Обновляем информацию хранящуюся в хранилище
            userStorage.getAllUsers().put(userId, user);
            userStorage.getAllUsers().put(friendId, friend);
        } catch (Exception e) {
            throw new ValidationException("Пользователь не удален из друзей, проверьте ID");
        }
    }

    // Метод возвращающий список общих друзей
    public List<User> allCoincideFriends(Long userId, Long friendId) {
        List<User> allFriendsWhichCoincide = new ArrayList<>(); // Список с совпадающими друзьями для возврата
        // Из хранилища получаю пользователя по ID
        User user = userStorage.getAllUsers().get(userId);
        // Получаем друга из хранилища
        User friend = userStorage.getAllUsers().get(friendId);
        // Проходимся циклом по всем ID друзей пользователя
        for (Long cycleUser : user.getFriendsID()) {
            // Проверяем совпадают ли ID друзей пользователя и ID друзей друга
            if (friend.getFriendsID().contains(cycleUser)) {
                // Добавляем пользователя в список с общими друзьями
                allFriendsWhichCoincide.add(userStorage.getAllUsers().get(cycleUser));
            }
        }
        return allFriendsWhichCoincide;
    }

    // Метод возвращающий список друзей пользователя
    public List<User> allFriendsOfUser(Long userId) {
        // Создаем возвращаемый список с друзьями пользователя
        List<User> allFriends = new ArrayList<>();
        // Из хранилища получаю пользователя по ID
        User user = userStorage.getAllUsers().get(userId);
        // Прохожусь циклом по множеству (id друзей) и из хранилища выдергиваю их и добавляю в возвращаемый список
        for (Long id : user.getFriendsID()) {
            allFriends.add(userStorage.getAllUsers().get(id));
        }
        return allFriends;
    }
}
