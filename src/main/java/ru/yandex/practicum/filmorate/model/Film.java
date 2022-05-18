package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.validators.AfterOrEqualData;

import javax.validation.constraints.*;
import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    @PositiveOrZero
    private int id; // Уникальеый индентификатор фильма
    @NotEmpty
    @NotNull
    private String name; // Название фильма
    @Size(min = 1, max = 200)
    private String description; // Описание фильма
    private Duration duration; // Продолжительность фильма
    @AfterOrEqualData("1895-12-28")
    private LocalDate releaseDate; // Дата выпуска фильма в прокат
}