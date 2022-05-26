package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;


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
    public User create(@Valid @RequestBody User user) throws Throwable {
        return userService.getUserStorage().create(user);
    }

    // Метод, который обновляет информацию по существующему пользователю или создает и добавляет нового пользователя
    @PutMapping
    public User update(@Valid @RequestBody User user) throws Throwable {
        return userService.getUserStorage().update(user);
    }

    // Метод удаляющий пользователя
    @DeleteMapping
    public void delete(@Valid @RequestBody User user) throws Throwable {
        userService.getUserStorage().delete(user);
    }

    // Метод по получению всех пользователей
    @GetMapping
    public List<User> getAllUsers() throws Throwable {
        return userService.getUserStorage().getAllUsers();
    }

    // Метод по получению одного пользователя (переменная пути)
    @GetMapping("/{id}")
    public User getOneUser(@PathVariable long id) throws Throwable {
        return userService.getUserStorage().getOneUser(id);
    }

    // Метод который добавляет пользователю нового друга
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) throws Throwable {
        userService.addFriend(id, friendId);
    }

    // Метод удаляющий друга из множества друзей пользователя
    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) throws Throwable {
        userService.deleteFromFriends(id, friendId);
    }

    // Метод возвращающий список друзей пользователя
    @GetMapping("/{id}/friends")
    public List<User> allFriendsOfUser (@PathVariable Long id) throws Throwable {
        return userService.allFriendsOfUser(id);
    }

    // Метод возвращает общий друзей (пользователя и друга)
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> allCoincideFriends (@PathVariable Long id, @PathVariable Long otherId) throws Throwable {
        return userService.allCoincideFriends(id, otherId);
    }
}
