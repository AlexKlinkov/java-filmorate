package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FilmDirector;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Data
@RequiredArgsConstructor
@Repository
public class FilmDirectorsDBStorage {

    private final JdbcTemplate jdbcTemplate;

    // Метод по добавлению фильма и режиссера в таблицу
    public void addFilmAndDirector(long filmId, long directorId) {
        jdbcTemplate.update("insert into films_of_director (directors_id, film_id) values (?, ?)",
                directorId, filmId);
    }

    // Метод по удалению фильма и режиссера из таблицы по ID фильма
    public void deleteFilmAndDirectorByFilmId(long directorId) {
        jdbcTemplate.update("delete from films_of_director where film_id = ?", directorId);
    }

    public Set<FilmDirector> getDirectorsByFilmId(long filmId) {
        String result = "select directors.id, directors.name from directors " +
                "left join films_of_director on films_of_director.directors_id = directors.id" +
                " left join film on film.id =  films_of_director.film_id where film.id = ?";
        Set<FilmDirector> directorResult = new HashSet<>();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(result, filmId);
        while (sqlRowSet.next()) {
            directorResult.add(new FilmDirector(sqlRowSet.getLong("id"),
                    sqlRowSet.getString("name")));
        }
        return directorResult;
    }
}
