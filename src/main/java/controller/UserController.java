package controller;

import MyException.ValidationException;
import controller.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    Comparator<Integer> comparatorID = new Comparator<>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2; // если отрицательное число, то первый объект меньше;
        }
    };
    Map<Integer, User> mapWithAllUsers = new TreeMap<>(comparatorID); // Мапа со всеми пользователями отсортированными
    // по ID, от меньшего к большему

    // Метод, который добавляет нового пользователя
    @PostMapping(value = "User")
    public void addUser(@RequestBody User user) throws ValidationException {
        if (user != null) { // Проверяем, что данный пользователь не пустые
            if (user.getEmail().isEmpty()) { // Если email пустой, выбрасываем исключение
                log.debug("Ошибка с email пользователя");
                throw new ValidationException("Почта пользователя не может быть пустым");
            }
            if (user.getEmail().contains("@")) { // Если email не содержит симфола '@', выбрасываем исключение
                log.debug("Ошибка с email пользователя");
                throw new ValidationException("Почта пользователя должна содержать символ '@'");
            }
            if (user.getLogin().isEmpty()) { // Если login пустой, выбрасываем исключение
                log.debug("Ошибка с login пользователя");
                throw new ValidationException("Login пользователя не может быть пустым");
            }
            if (user.getLogin().contains(" ")) { // Если login содержит пробел, выбрасываем исключение
                log.debug("Ошибка с login пользователя");
                throw new ValidationException("Login пользователя не должен быть с пробелами");
            }
            if (user.getName().isEmpty()) { // Если имя пустое, то в качестве имени используем login
                log.debug("Имя было пустое и в качестве имени мы взяли login");
                user.setName(user.getLogin());
            }
            if (user.getBirthday().isAfter(LocalDate.now())) { // Если дата рождения в будущем
                log.debug("Ошибка с датой рождения пользователя");
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
            log.debug("Новый пользователь успешно добавлен");
            mapWithAllUsers.put(user.getId(), user);
        } else { // Если пользователь пустое значение выбрасываем ошибку 400
            throw new ValidationException(HttpStatus.BAD_REQUEST.toString());
        }
    }

    // Метод, который обновляет информацию по существующему пользователю или создает и добавляет нового пользователя
    @PutMapping(value = "User")
    public void updateUser(@RequestBody User user) throws ValidationException {
        if (user != null) { // Проверяем, что данный пользователь не пустые
            // Обновляем информацию по существующему пользователю
            // Так как у нас все фильмы имеют ID в мапе в качестве ключа, то алгорит будет не O(n), как при структуре
            // данных СПИСОК ИЛИ МНОЖЕСТВО, где пришлось бы делать циклы, а O(1)
            if (mapWithAllUsers.containsKey(user.getId())) { // Если пользователь с таким ID уже существует
                int id = user.getId(); // ID существующего фильма
                // Для существующего пользователя обновляем email
                log.debug("Email пользователя успешно обновлен");
                mapWithAllUsers.get(id).setEmail(user.getEmail());
                // Для существующего пользователя обновляем login
                log.debug("Login пользователя успешно обновлен");
                mapWithAllUsers.get(id).setLogin(user.getLogin());
                // Для существующего пользователя обновляем Имя
                log.debug("Имя пользователя успешно обновлено");
                mapWithAllUsers.get(id).setName(user.getName());
                // Для существующего пользователя обновляем дату рождения
                log.debug("Дата рождения пользователя успешно обновлена");
                mapWithAllUsers.get(id).setBirthday(user.getBirthday());
                return;
                // Если пользователь еще не зарегистрирован по ID, создаем нового
            } else {
                log.debug("Новый пользователь успешно добавлен");
                addUser(user);
            }
        } else { // Если пользователь пустое значение выбрасываем ошибку 400
            throw new ValidationException(HttpStatus.BAD_REQUEST.toString());
        }
    }

    // Метод по получению всех пользователей
    @GetMapping
    public Map<Integer, User> getAllUsers() {
        log.debug("Все пользователи были успешно переданы клиенту");
        return mapWithAllUsers;
    }
}
