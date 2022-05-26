package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

public interface UserStorage {

    User create(User user) throws Throwable; // Метод создающий/Добавляющий пользователя в хранилище
    User update(User user) throws Throwable; // Метод обновляющий пользователя или если такого пользователя нет, создает нового (Модификация)
    void delete(User user) throws Throwable; // Метод удаляющий пользователя
    List<User> getAllUsers() throws Throwable; // Метод по возвращению всех пользователей
    User getOneUser(long id) throws Throwable; // Метод возвращающий пользователя по ID
}
