package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundExceptionFilmorate;
import ru.yandex.practicum.filmorate.exception.ValidationExceptionFilmorate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Slf4j
@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmsGenresDbStorage filmsGenresDbStorage;
    private final MPADbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final LikeStatusDbStorage likeStatusDbStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmsGenresDbStorage filmsGenresDbStorage, MPADbStorage mpaDbStorage, GenreDbStorage genreDbStorage, LikeStatusDbStorage likeStatusDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmsGenresDbStorage = filmsGenresDbStorage;
        this.mpaDbStorage = mpaDbStorage;
        this.genreDbStorage = genreDbStorage;
        this.likeStatusDbStorage = likeStatusDbStorage;
    }

    @Override
    public Film create(Film film) throws RuntimeException {
        if (film == null) {
            log.debug("При попытке создать новый фильм произошла ошибка с NULL");
            throw new NotFoundExceptionFilmorate("Искомый объект не найден");
        }
        log.debug("При создании фильма проверяем, что данного фильма еще нет в БД");
        SqlRowSet alreadyExist = jdbcTemplate.queryForRowSet("select * from FILM where NAME = ? " +
                        "AND DESCRIPTION = ? AND RELEASE_DATE = ?",
                film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()));
        if (alreadyExist.first()) {
            log.debug("Если фильм уже есть в БД, то не создаем его, а возвращаем из БД, " +
                    "обеспечивая уникальность данных");
            alreadyExist.beforeFirst();
            while (alreadyExist.next()) {
                Film returnFilm = getFilmById(alreadyExist.getLong("ID"));
                if (returnFilm.getGenres() == null || returnFilm.getGenres().isEmpty()) {
                    returnFilm.setGenres(null);
                }
                return returnFilm;
            }
        }
        try {
            log.debug("Возвращаем и добавляем фильм в БД");
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("FILM").usingGeneratedKeyColumns("ID");
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("NAME", film.getName())
                    .addValue("DESCRIPTION", film.getDescription())
                    .addValue("DURATION", film.getDuration())
                    .addValue("RELEASE_DATE", Date.valueOf(film.getReleaseDate()))
                    .addValue("RATE", film.getRate())
                    .addValue("MPA_ID", film.getMpa().getId());
            Number num = jdbcInsert.executeAndReturnKey(parameters);
            film.setId(num.intValue());
            log.debug("При создании фильма запоняем возрастного рейтинга по ID");
            MPA mpa = mpaDbStorage.getMPAById(film.getMpa().getId());
            film.setMpa(mpa);
            if (film.getGenres() != null && !(film.getGenres().isEmpty())) {
                Set<Genre> genres = film.getGenres();
                for (Genre genre : genres) {
                    log.debug("Заполняем таблицу Film_genre при создании объекта");
                    filmsGenresDbStorage.addFilmAndGenre(film.getId(), genre.getId());
                }
            } else {
                film.setGenres(null);
            }
            return film;
        } catch (RuntimeException e) {
            log.debug("При попытке создать новый фильм произошла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    @Override
    public Film update(Film film) throws RuntimeException {
        if (film == null) {
            log.debug("При обновлении фильма передали значение Null");
            throw new ValidationExceptionFilmorate("Ошибка валидации");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from FILM where ID = ?", film.getId());
        if (!sqlRowSet.first()) {
            log.debug("При обновлении фильма объект с ID - " + film.getId() + " не был найден");
            throw new NotFoundExceptionFilmorate("Искомый объект не найден");
        } else {
            try {
                String sqlQuery = "UPDATE FILM SET " +
                        "NAME = ?, DESCRIPTION = ?, DURATION = ?, RELEASE_DATE = ?, RATE = ?, MPA_ID = ?" +
                        " where  ID = ?";
                jdbcTemplate.update(sqlQuery,
                        film.getName(), film.getDescription(), film.getDuration(),
                        Date.valueOf(film.getReleaseDate()), film.getRate(), film.getMpa().getId(), film.getId());
                log.debug("Обновляем данные в таблице film_genre");
                if (film.getGenres() != null && !(film.getGenres().isEmpty())) {
                    log.debug("Удаляем предыдущие данные из таблицы FILM_GENRE при обновление фильма");
                    filmsGenresDbStorage.deleteFilmAndGenreByFilmId(film.getId());
                    log.debug("Вставляем новые данные в таблицу FILM_GENRE при обновление фильма");
                    Set<Genre> genres = new HashSet<>(film.getGenres());
                    for (Genre genre : genres) {
                        filmsGenresDbStorage.addFilmAndGenre(film.getId(), genre.getId());
                    }
                    log.debug("заполняем поле жанров фильма при обновлении фильма");
                    film.setGenres(genreDbStorage.getGenresByFilmId(film.getId()));
                } else {
                    log.debug("Удаляем предыдущие данные из таблицы FILM_GENRE если поле с жанрами " +
                            "оказалось пустым при обновление фильма");
                    filmsGenresDbStorage.deleteFilmAndGenreByFilmId(film.getId());
                    if (film.getGenres() != null) {
                        film.setGenres(new HashSet<>());
                    } else {
                        film.setGenres(null);
                    }
                }
                log.debug("Заполняем поле возрастного рейтинга (имя) при обновлении фильма");
                film.setMpa(mpaDbStorage.getMPAById(film.getMpa().getId()));
                return film;
            } catch (RuntimeException e) {
                log.debug("При обновлении фильма возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    @Override
    public void delete(Film film) throws RuntimeException {
        if (film == null) {
            log.debug("При удаления фильма возникла ошибка с NULL");
            throw new NotFoundExceptionFilmorate("Искомый объект не найден");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from FILM where ID = ?", film.getId());
        if (!sqlRowSet.first()) {
            log.debug("При удалении фильма возникла ошибка с ID");
            throw new ValidationExceptionFilmorate("Ошибка валидации");
        } else {
            try {
                log.debug("Удалили фильм");
                filmsGenresDbStorage.deleteFilmAndGenreByFilmId(film.getId());
                likeStatusDbStorage.deleteLikeByFilmId(film.getId());
                jdbcTemplate.update("delete from FILM where ID = ?", film.getId());
            } catch (RuntimeException e) {
                log.debug("При удалении фильма возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    @Override
    public List<Film> getFilms() throws RuntimeException {
        try {
            log.debug("Возвращаем список со всеми фильмами");
            String sqlQuery = "SELECT * from  FILM " +
                    "LEFT JOIN MPA ON FILM.MPA_ID = MPA.ID;";
            return jdbcTemplate.query(sqlQuery, this::makeFilm);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    @Override
    public Film getFilmById(long id) throws RuntimeException {
        if (id < 0) {
            log.debug("При попытке вернуть фильм возникла ошибка с ID");
            throw new NotFoundExceptionFilmorate("Искомый объект не найден");
        }
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from FILM where ID = ?", id);
        if (!filmRows.first()) {
            log.debug("При получении фильма возникла ошибка с NULL");
            throw new ValidationExceptionFilmorate("Ошибка валидации");
        } else {
            try {
                List<Film> films = getFilms();
                for (Film film : films) {
                    if (film.getId() == id) {
                        Film returnFilm = getFilms().get(new ArrayList<>(films).indexOf(film));
                        if (returnFilm.getGenres().isEmpty()) {
                            returnFilm.setGenres(null);
                        }
                        return returnFilm;
                    }
                }
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть фильм возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
        return null;
    }

    private Film makeFilm(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Собираем объект в методе makeFilm");
        Film film = new Film(
                resultSet.getLong("FILM.ID"),
                resultSet.getString("FILM.NAME"),
                resultSet.getString("FILM.DESCRIPTION"),
                resultSet.getLong("FILM.DURATION"),
                resultSet.getDate("FILM.RELEASE_DATE").toLocalDate(),
                new MPA(resultSet.getInt("MPA.ID"), resultSet.getString("MPA.NAME")),
                Set.of(new Genre(0, "EMPTY")),
                resultSet.getLong("RATE")
        );
        Set<Genre> genres = genreDbStorage.getGenresByFilmId(film.getId());
        if (!genres.isEmpty()) {
            Set<Genre> sortedGenres = new TreeSet<>(Comparator.comparing(Genre::getId));
            sortedGenres.addAll(genres);
            genres = sortedGenres;
        }
        film.setGenres(genres);
        return film;
    }

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) { // получить Общие фильмы
        String sqlQuery = "SELECT film_id FROM like_status WHERE user_id = ? INTERSECT " +
                "SELECT film_id FROM like_status WHERE user_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, userId, friendId);
        List<Film> commonFilms = new ArrayList<>();
        while (rowSet.next()) {
            commonFilms.add(getFilmById(rowSet.getInt("film_id")));
        }
        return commonFilms;
    }
}
