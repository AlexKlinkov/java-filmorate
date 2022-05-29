package ru.yandex.practicum.filmorate.model;


import lombok.Data;
import lombok.NonNull;


import ru.yandex.practicum.filmorate.validators.AfterOrEqualData;

import javax.validation.constraints.*;

import java.time.LocalDate;

import java.util.HashSet;

import java.util.Set;

@Data
public class Film {
    transient private long id; // Уникальеый индентификатор фильма
    @NotEmpty
    @NotNull
    private String name; // Название фильма
    @Size(min = 1, max = 200)
    private String description; // Описание фильма
    @Positive
    private Long duration; // Продолжительность фильма
    @AfterOrEqualData("1895-12-28")
    private LocalDate releaseDate; // Дата выпуска фильма в прокат
    @NonNull
    private Set<Long> setWithLike = new HashSet<>(); // множество лайков к фильму
    @NonNull
    private Long rate;

    public Film(String name, String description, Long duration, LocalDate releaseDate) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.releaseDate = releaseDate;
    }
}
