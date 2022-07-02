package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDirector;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
public class FilmDirectorsDBStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDirectorsDBStorage (JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }
    // Метод по добавлению фильма и режиссера в таблицу
    public void addFilmAndDirector (long filmId,long directorId) {
        jdbcTemplate.update("INSERT INTO films_of_director (directors_id, film_id) VALUES (?, ?)",
                directorId, filmId);
    }
    // Метод по удалению фильма и режиссера из таблицы по ID фильма
    public void deleteFilmAndDirectorByFilmId (long directorId) {
        jdbcTemplate.update("delete from films_of_director where FILM_ID = ?", directorId);
    }

    public Set<FilmDirector> getDirectorsByFilmId (long filmId) {
        String result = "SELECT DIRECTORS.ID, DIRECTORS.NAME FROM DIRECTORS " +
                "LEFT JOIN films_of_director on films_of_director.directors_id = DIRECTORS.ID" +
                " left join FILM on FILM.ID =  films_of_director.FILM_ID where FILM.ID = ?";
        Set<FilmDirector> directorResult = new HashSet<>();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(result, filmId);
        while (sqlRowSet.next()) {
            directorResult.add(new FilmDirector(sqlRowSet.getLong("ID"),
                    sqlRowSet.getString("NAME")));
        }
        return directorResult;
    }


}
