package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;

@Slf4j
@Component
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getGenres() {
        try {
            log.debug("Возвращаем список с жанрами");
            return jdbcTemplate.query("select * from GENRE", this::makeGenre);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми жанрами возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    public Genre getOneById (long id) {
        if (id < 0) {
            log.debug("При получении жанра возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        Genre genre = null;
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from GENRE where ID = ?", id);
        if (sqlRowSet.next()) {
            genre =  new Genre(sqlRowSet.getInt("ID"), sqlRowSet.getString("NAME"));
        }
        return genre;
    }

    public TreeSet<Genre> getGenresByFilmId (long filmId) {
        String result = "SELECT GENRE.ID, GENRE.NAME FROM GENRE " +
                "LEFT JOIN FILM_GENRE on FILM_GENRE.GENRE_ID = GENRE.ID " +
                " left join FILM on FILM_GENRE.FILM_ID = FILM.ID where FILM.ID = ?";
        TreeSet<Genre> genresResult = new TreeSet<>();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(result, filmId);
        while (sqlRowSet.next()) {
            genresResult.add(new Genre(sqlRowSet.getInt("ID"),
                    sqlRowSet.getString("NAME")));
        }
        return genresResult;
    }

    public Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Собираем объект в методе makeGenre");
        return new Genre(
                resultSet.getInt("ID"),
                resultSet.getString("NAME")
        );
    }
}
