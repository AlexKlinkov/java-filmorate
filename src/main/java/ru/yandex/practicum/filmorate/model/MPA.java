package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class MPA {
    private long id;
    private String name;

    public MPA(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
