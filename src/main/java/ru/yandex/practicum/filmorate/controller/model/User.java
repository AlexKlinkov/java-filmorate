package ru.yandex.practicum.filmorate.controller.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private int id; // Целочисленный идентификатор
    private String email; // Электронная почта
    private String login; // Логин пользователя
    private String name; // Имя для отображения
    private LocalDate birthday; // Дата рождения
}
