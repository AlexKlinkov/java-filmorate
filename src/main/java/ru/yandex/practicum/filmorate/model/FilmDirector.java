package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;


@Data
public class FilmDirector {
    long id;
    @NotEmpty
    @NotBlank
    String name;

    public FilmDirector(long id, String name) {
        this.id = id;
        this.name = name;
    }
}

