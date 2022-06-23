package ru.yandex.practicum.filmorate.model;

import lombok.Data;


@Data
public class MPA {
    private int id;
    private String name;

    public MPA(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
