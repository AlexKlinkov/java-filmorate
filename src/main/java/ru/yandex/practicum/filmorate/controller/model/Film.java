package ru.yandex.practicum.filmorate.controller.model;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    private int id; // Уникальеый индентификатор фильма
    private String name; // Название фильма
    private String description; // Описание фильма
    private LocalDate releaseDate; // Дата выпуска фильма в прокат
    private Duration duration; // Продолжительность фильма
}
