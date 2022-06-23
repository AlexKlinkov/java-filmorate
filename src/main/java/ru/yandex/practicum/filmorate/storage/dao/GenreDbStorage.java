package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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

    public Genre getGenreById(int id) {
        if (id < 0) {
            log.debug("При попытке вернуть жанр возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from GENRE where ID = ?", id);
        if (!filmRows.first()) {
            log.debug("При получении жанра возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                return getGenres().get(0);
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть жанр возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    public Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Собираем объект в методе makeGenre");
        return new Genre(
            resultSet.getInt("ID"),
            resultSet.getString("NAME")
        );
    }
}
