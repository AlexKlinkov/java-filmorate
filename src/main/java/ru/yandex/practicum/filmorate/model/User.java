package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class User {
    @PositiveOrZero
    private long id; // Целочисленный идентификатор
    @Email
    private String email; // Электронная почта
    @NotNull
    @Pattern(regexp = "^\\S*$")
    private String login; // Логин пользователя
    private String name; // Имя для отображения
    @Past
    private LocalDate birthday; // Дата рождения
    private Set<Long> friendsID = new LinkedHashSet<>(); // Поле для хранения id всех друзей
}
