package ru.yandex.practicum.filmorate.storage.user;


import ru.yandex.practicum.filmorate.model.User;

import java.util.List;


public interface UserStorage {

    User create(User user) throws RuntimeException; // Метод создающий/Добавляющий пользователя в хранилище

    User update(User user) throws RuntimeException; // Метод обновляющий пользователя или если такого пользователя

    // нет, создает нового (Модификация)
    void delete(User user) throws RuntimeException; // Метод удаляющий пользователя

    List<User> getUsers() throws RuntimeException; // Метод по возвращению всех пользователей

    User getUserById(long id) throws RuntimeException; // Метод возвращающий пользователя по ID
}
