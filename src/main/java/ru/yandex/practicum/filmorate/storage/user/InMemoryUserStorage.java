package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    Map<Long, User> mapWithAllUsers = new HashMap<>();

    // Метод по созданию/добавлению нового пользователя
    @Override
    public User create(User user) {
        if (user == null) {
            log.debug("При попытке создать нового пользователя произошла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        } else {
            log.debug("Устанавливаем автоматически ID для пользователя");
            user.setId(mapWithAllUsers.size() + 1);
        }
        try {
            if (user.getName().isEmpty()) {
                log.debug("Имя было пустое и в качестве имени мы взяли login");
                user.setName(user.getLogin());
            }
            log.debug("Новый пользователь успешно создан/добавлен");
            mapWithAllUsers.put(user.getId(), user);
            return mapWithAllUsers.get(user.getId());
        } catch (RuntimeException e) {
            log.debug("При попытке создать нового пользователя произошла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    // Метод, который обновляет информацию по существующему пользователю
    @Override
    public User update(User user) {
        if (user == null) {
            log.debug("При обновлении пользователя передали значение Null");
            throw new ValidationException("Ошибка валидации");
        }
        if (user.getId() < 0 || mapWithAllUsers.get(user.getId()) == null) {
            log.debug("При обновлении пользователя объект с ID - " + user.getId() + " не был найден");
            throw new NotFoundException("Искомый объект не найден");
        } else {
            try {
                log.debug("Обновляем информацию по пользователю через ID");
                mapWithAllUsers.put(user.getId(), user);
                return mapWithAllUsers.get(user.getId());
            } catch (RuntimeException e) {
                log.debug("При обновлении пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    // Метод, который удаляет пользователя
    @Override
    public void delete(User user) {
        if (user == null) {
            log.debug("При удалении пользователя возникла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        }
        if (user.getId() < 0 || mapWithAllUsers.get(user.getId()) == null) {
            log.debug("При удалении пользователя возникла ошибка с ID");
            throw new ValidationException("Ошибка валидации");
        } else if (mapWithAllUsers.containsValue(user)) {
            try {
                log.debug("Пытаемся удалить пользователя");
                mapWithAllUsers.remove(user.getId());
            } catch (RuntimeException e) {
                log.debug("При удалении пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    // Метод по возвращению всех пользователей
    @Override
    public List<User> getUsers() {
        try {
            log.debug("Пытаемся вернуть список всех пользователей");
            return new ArrayList<>(mapWithAllUsers.values());
        } catch (RuntimeException exception) {
            log.debug("При попытке вернуть список со всеми пользователями возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    // Метод возвращающий пользователя одного по ID
    @Override
    public User getUserById(long id) {
        if (id < 0) {
            log.debug("При попытке вернуть пользователя возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        if (mapWithAllUsers.get(id) == null) {
            log.debug("При получения пользователя возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Пытаюсь вернуть одного пользователя");
                return mapWithAllUsers.get(id);
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }
}
