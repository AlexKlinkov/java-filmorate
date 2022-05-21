package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Map;

public interface UserStorage {

    User create(User user) throws ValidationException; // Метод создающий/Добавляющий пользователя в хранилище
    void update(User user) throws ValidationException; // Метод обновляющий пользователя или если такого пользователя нет, создает нового (Модификация)
    void delete(User user); // Метод удаляющий пользователя
    Map<Long, User> getAllUsers(); // Метод по возвращению всех пользователей
}
