package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.validators.AfterOrEqualData;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;

@Data
public class Film {
    transient long id; // Уникальеый индентификатор фильма
    @NotEmpty
    @NotNull
    private String name; // Название фильма
    @Size(min = 1, max = 200)
    private String description; // Описание фильма
    @Positive
    private long duration; // Продолжительность фильма
    @AfterOrEqualData("1895-12-28")
    private LocalDate releaseDate; // Дата выпуска фильма в прокат
    private Set<Long> amountOfLIke; // множество лайков к фильму
    private MPA mpa; // Возрастной рейтинг фильма
    private Set<Genre> genres; // множество с жанрами фильма
    private long rate;
    private Set<FilmDirector> directors;


    public Film(long id, String name, String description, Long duration,
                LocalDate releaseDate, MPA mpa, Set<Genre> genres, long rate, Set<FilmDirector> directors) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.mpa = mpa;
        this.genres = genres;
        this.rate = rate;
        this.amountOfLIke = new HashSet<>();
        this.directors = directors;
    }
}
