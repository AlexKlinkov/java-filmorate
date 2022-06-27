package ru.yandex.practicum.filmorate.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
@Data
public class User {
    transient long id; // Целочисленный идентификатор
    @Email
    private String email; // Электронная почта
    @NotNull
    @Pattern(regexp = "^\\S*$")
    private String login; // Логин пользователя
    transient private String name; // Имя для отображения
    @Past
    private LocalDate birthday; // Дата рождения
    Set<Long> friendsID; // Поле для хранения id всех друзей

    public User(long id, String email, String login, String name,
                LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        if (friendsID == null) {
            this.friendsID = new HashSet<>();
        }
    }

    @JsonCreator
    public User(String email, String login, String name,
                LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        if (friendsID == null) {
            this.friendsID = new HashSet<>();
        }
    }
}
