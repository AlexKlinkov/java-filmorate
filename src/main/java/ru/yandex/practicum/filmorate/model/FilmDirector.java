package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;


@Data
public class FilmDirector {
    Long id;
    @NotEmpty
    @NotBlank
    String name;

    public FilmDirector(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}

