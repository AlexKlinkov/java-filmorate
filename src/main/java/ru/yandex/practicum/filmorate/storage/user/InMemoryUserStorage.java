package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    Map<Long, User> mapWithAllUsers = new HashMap<>(); // Мапа со всеми пользователями

    // Метод, который обновляет информацию по существующему пользователю или создает и добавляет нового пользователя
    @Override
    public User create(User user) throws ValidationException {
        if (user != null) { // Проверяем, что данный пользователь не пустые
            if (user.getName().isEmpty()) { // Если имя пустое, то в качестве имени используем login
                log.debug("Имя было пустое и в качестве имени мы взяли login");
                user.setName(user.getLogin());
            }
            log.debug("Новый пользователь успешно добавлен");
            mapWithAllUsers.put(user.getId(), user);
            return mapWithAllUsers.get(user.getId());
        } else { // Если пользователь пустое значение выбрасываем ошибку 400
            throw new ValidationException(HttpStatus.BAD_REQUEST.toString());
        }
    }

    // Метод, который обновляет информацию по существующему пользователю или создает и добавляет нового пользователя
    @Override
    public void update(User user) throws ValidationException {
        if (user != null) { // Проверяем, что данный пользователь не пустые
            // Обновляем информацию по существующему пользователю
            // Так как у нас все фильмы имеют ID в мапе в качестве ключа, то алгорит будет не O(n), как при структуре
            // данных СПИСОК ИЛИ МНОЖЕСТВО, где пришлось бы делать циклы, а O(1)
            if (mapWithAllUsers.containsKey(user.getId())) { // Если пользователь с таким ID уже существует
                User userFromMap = mapWithAllUsers.get(user.getId()); // Существующего пользователя взяли из мапы
                // Для существующего пользователя обновляем email
                log.debug("Email пользователя успешно обновлен");
                userFromMap.setEmail(user.getEmail());
                // Для существующего пользователя обновляем login
                log.debug("Login пользователя успешно обновлен");
                userFromMap.setLogin(user.getLogin());
                // Для существующего пользователя обновляем Имя
                log.debug("Имя пользователя успешно обновлено");
                userFromMap.setName(user.getName());
                // Для существующего пользователя обновляем дату рождения
                log.debug("Дата рождения пользователя успешно обновлена");
                userFromMap.setBirthday(user.getBirthday());
                return;
                // Если пользователь еще не зарегистрирован по ID, создаем нового
            } else {
                log.debug("Пользователя для обновления нет, пытаемся его создать");
                create(user);
            }
        } else { // Если пользователь пустое значение выбрасываем ошибку 400
            throw new ValidationException(HttpStatus.BAD_REQUEST.toString());
        }
    }

    // Метод, который удаляет пользователя
    @Override
    public void delete(User user) {
        mapWithAllUsers.remove(user.getId());
    }

    // Метод по возвращению всех пользователей
    @Override
    public Map<Long, User> getAllUsers() {
        return mapWithAllUsers;
    }
}
