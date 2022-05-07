package ru.yandex.practicum.filmorate.controller.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    @PositiveOrZero
    private int id; // Уникальеый индентификатор фильма
    @NotEmpty
    private String name; // Название фильма
    @Size(min = 1, max = 200)
    private String description; // Описание фильма
    private Duration duration; // Продолжительность фильма
    private LocalDate releaseDate; // Дата выпуска фильма в прокат
}
