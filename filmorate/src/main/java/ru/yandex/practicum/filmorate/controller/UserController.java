package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Метод, который добавляет нового пользователя
    @PostMapping
    public User create(@Valid @RequestBody User user) throws RuntimeException, SQLException {
        return userService.getUserStorage().create(user);
    }

    // Метод, который обновляет информацию по существующему пользователю или создает и добавляет нового пользователя
    @PutMapping
    public User update(@Valid @RequestBody User user) throws RuntimeException, SQLException {
        return userService.getUserStorage().update(user);
    }

    // Метод удаляющий пользователя
    @DeleteMapping
    public void delete(@Valid @RequestBody User user) throws RuntimeException {
        userService.getUserStorage().delete(user);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        userService.getUserStorage().deleteById(id);
    }

    // Метод по получению всех пользователей
    @GetMapping
    public List<User> getAll() throws RuntimeException {
        return userService.getUserStorage().getUsers();
    }

    // Метод по получению одного пользователя (переменная пути)
    @GetMapping("/{id}")
    public User getOne(@PathVariable long id) throws RuntimeException, SQLException {
        return userService.getUserStorage().getUserById(id);
    }

    // Метод который добавляет пользователю нового друга
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) throws RuntimeException, SQLException {
        userService.addFriend(id, friendId);
    }

    // Метод удаляющий друга из множества друзей пользователя
    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) throws RuntimeException, SQLException {
        userService.deleteFromFriends(id, friendId);
    }

    // Метод возвращающий список друзей пользователя
    @GetMapping("/{id}/friends")
    public List<User> allFriendsOfUser(@PathVariable Long id) throws RuntimeException, SQLException {
        return userService.allFriendsOfUser(id);
    }

    // Метод возвращает общий друзей (пользователя и друга)
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> allCoincideFriends(@PathVariable Long id, @PathVariable Long otherId)
            throws RuntimeException, SQLException {
        return userService.allCoincideFriends(id, otherId);
    }

    // Метод возвращает список рекомендуемых фильмов для пользователя
    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable Long id) throws RuntimeException, SQLException {
        return userService.getRecommendations(id);
    }
}
