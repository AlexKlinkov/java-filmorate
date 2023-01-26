package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
public class FilmDirector {
    private long id;
    @NotEmpty
    @NotBlank
    private String name;

    public FilmDirector(long id, String name) {
        this.id = id;
        this.name = name;
    }
}

