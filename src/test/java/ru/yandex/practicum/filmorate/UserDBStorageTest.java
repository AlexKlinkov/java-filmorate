package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class UserDBStorageTest {
    private final UserDbStorage userStorage;
    private User user = new User(0L, "1kot@mail.ru", "KotoMax",
            "Vasia", LocalDate.of(1193, 03, 25));

    @Autowired
    UserDBStorageTest(UserDbStorage userStorage) {
        this.userStorage = userStorage;
    }

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void init() throws SQLException {
        userStorage.create(user);
    }

    @AfterEach
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void del() {
        userStorage.deleteById(user.getId());
    }

    @Test
    public void testUpdateUser() throws SQLException {
        User newUser = new User(user.getId(), "1kot@mail.ru", "NEWSWOMAN",
                "Vasa", LocalDate.of(1993, 3, 25));
        userStorage.update(newUser);
        Assertions.assertEquals("NEWSWOMAN", userStorage.getUserById(newUser.getId()).getLogin());
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void testGetUsers() {
        List<User> users = new ArrayList<>(userStorage.getUsers());
        Assertions.assertEquals(1, users.size());
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void testFindUserById() throws SQLException {
        Assertions.assertNotNull(userStorage.getUserById(user.getId()));
    }
}

