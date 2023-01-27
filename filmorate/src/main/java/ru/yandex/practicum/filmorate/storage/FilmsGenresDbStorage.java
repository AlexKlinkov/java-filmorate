package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Data
@RequiredArgsConstructor
@Repository
public class FilmsGenresDbStorage {

    private final JdbcTemplate jdbcTemplate;

    // Метод по добавлению фильма и жанра в таблицу
    public void addFilmAndGenre(long filmId, int genreId) {
        jdbcTemplate.update("insert into film_genre (film_id, genre_id) values (?, ?)",
                filmId, genreId);
    }

    // Метод по удалению фильма и жанра из таблицы по ID фильма
    public void deleteFilmAndGenreByFilmId(long filmId) {
        jdbcTemplate.update("delete from film_genre where film_id = ?", filmId);
    }
}
