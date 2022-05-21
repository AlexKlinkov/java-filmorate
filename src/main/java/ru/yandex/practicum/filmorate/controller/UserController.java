package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;


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
    public User create(@Valid @RequestBody User user) throws ValidationException {
        return userService.getUserStorage().create(user);
    }

    // Метод, который обновляет информацию по существующему пользователю или создает и добавляет нового пользователя
    @PutMapping
    public void update(@Valid @RequestBody User user) throws ValidationException {
        userService.getUserStorage().update(user);
    }

    // Метод удаляющий пользователя
    @DeleteMapping
    public void delete(@Valid @RequestBody User user) {
        userService.getUserStorage().delete(user);
    }

    // Метод по получению всех пользователей
    @GetMapping
    public Map<Long, User> getAllUsers() {
        return userService.getUserStorage().getAllUsers();
    }

    // Метод по получению одного пользователя (переменная пути)
    @GetMapping("/{id}")
    public User getOneUser(@Valid @PathVariable Long id) {
        return userService.getUserStorage().getAllUsers().get(id);
    }

    // Метод который добавляет пользователю нового друга
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@Valid @PathVariable Long id, @PathVariable Long friendId) throws ValidationException {
        userService.addFriend(id, friendId);
    }

    // Метод удаляющий друга из множества друзей пользователя
    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@Valid @PathVariable Long id, @PathVariable Long friendId) throws ValidationException {
        userService.deleteFromFriends(id, friendId);
    }

    // Метод возвращающий список друзей пользователя
    @GetMapping("/{id}/friends")
    public List<User> allFriendsOfUser (@Valid @PathVariable Long id) {
        return userService.allFriendsOfUser(id);
    }

    // Метод возвращает общий друзей (пользователя и друга)
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> allCoincideFriends (@Valid @PathVariable Long id, @Valid @PathVariable Long otherId) {
        return userService.allCoincideFriends(id, otherId);
    }
}
