package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;

@Slf4j
@Data
@RequiredArgsConstructor
@Repository
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Genre> getGenres() {
        try {
            log.debug("Возвращаем список с жанрами");
            return jdbcTemplate.query("select * from genre", this::makeGenre);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми жанрами возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    public Genre getOneById(long id) {
        if (id < 0) {
            log.debug("При получении жанра возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        Genre genre = null;
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from genre where id = ?", id);
        if (sqlRowSet.next()) {
            genre = new Genre(sqlRowSet.getInt("id"), sqlRowSet.getString("name"));
        }
        return genre;
    }

    public TreeSet<Genre> getGenresByFilmId(long filmId) {
        String result = "select genre.id, genre.name from genre " +
                "left join film_genre on film_genre.genre_id = genre.id " +
                " left join film on film_genre.film_id = film.id where film.id = ?";
        TreeSet<Genre> genresResult = new TreeSet<>();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(result, filmId);
        while (sqlRowSet.next()) {
            genresResult.add(new Genre(sqlRowSet.getInt("id"),
                    sqlRowSet.getString("name")));
        }
        return genresResult;
    }

    public Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Собираем объект в методе makeGenre");
        return new Genre(
                resultSet.getInt("id"),
                resultSet.getString("name")
        );
    }
}
