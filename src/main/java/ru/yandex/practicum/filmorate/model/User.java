package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {
    @PositiveOrZero
    private int id; // Целочисленный идентификатор
    @Email
    private String email; // Электронная почта
    @NotNull
    @Pattern(regexp = "^\\S*$")
    private String login; // Логин пользователя
    private String name; // Имя для отображения
    @Past
    private LocalDate birthday; // Дата рождения
}
