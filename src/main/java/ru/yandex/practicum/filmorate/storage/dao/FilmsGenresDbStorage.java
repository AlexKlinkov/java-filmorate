package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class FilmsGenresDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmsGenresDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
        // Метод по добавлению фильма и жанра в таблицу
    public void addFilmAndGenre (long filmId, int genreId) {
        jdbcTemplate.update("INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)",
                filmId, genreId);
    }
        // Метод по удалению фильма и жанра из таблицы по ID фильма
    public void deleteFilmAndGenreByFilmId (long filmId) {
        jdbcTemplate.update("delete from FILM_GENRE where FILM_ID = ?", filmId);
    }
}
