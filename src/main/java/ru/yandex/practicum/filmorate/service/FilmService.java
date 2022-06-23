package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Service
public class FilmService {

    private final FilmStorage filmStorage; // Хранилище с фильмами
    private final UserStorage userStorage; // Хранилище с пользователями
    private final JdbcTemplate jdbcTemplate; // Объект для работы с БД

    // Внедряем доступ сервиса к хранилищу с фильмами
    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                       @Qualifier("UserDbStorage") UserStorage userStorage, JdbcTemplate jdbcTemplate) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    // Метод по добавлению лайка
    public void addLike(Long filmId, Long userId) {
        if (filmId < 0 || userId < 0) {
            log.debug("При добавлении лайка возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        } else if (filmStorage.getFilmById(filmId) == null || userStorage.getUserById(userId) == null) {
            log.debug("При добавлении лайка возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Делаем запись в таблицу like_status");
                jdbcTemplate.update("MERGE INTO LIKE_STATUS (FILM_ID, USER_ID) " +
                        "VALUES (?, ?)", filmId, userId);
                log.debug("Получаем фильм из БД");
                Film film = filmStorage.getFilmById(filmId);
                log.debug("Увеличиваем пользовательский рейтинг фильма на 1");
                film.setRate(film.getRate() + 1);
                log.debug("Обновляем фильм в БД при добавлении лайка");
                filmStorage.update(film);
            } catch (RuntimeException e) {
                log.debug("При добавлении лайка к фильму возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    // Метод по удалению лайка
    public void deleteLike(Long filmId, Long userId) {
        if (filmId < 0 || userId < 0) {
            log.debug("При удалении лайка возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        } else if (filmStorage.getFilmById(filmId) == null || userStorage.getUserById(userId) == null) {
            log.debug("При удалении лайка возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Удаляем запись из таблицы like_status если такая запись существует");
                SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from LIKE_STATUS where FILM_ID = ? " +
                        "AND USER_ID = ?", filmId, userId);
                if (sqlRowSet.first()) {
                    jdbcTemplate.update("DELETE FROM LIKE_STATUS where FILM_ID = ? AND USER_ID = ? "
                            , filmId, userId);
                    log.debug("Получаем фильм из БД");
                    Film film = filmStorage.getFilmById(filmId);
                    log.debug("Уменьшаем пользовательский рейтинг фильма на 1");
                    film.setRate(film.getRate() - 1);
                    log.debug("Обновляем фильм в БД при удалении лайка");
                    filmStorage.update(film);
                }
            } catch (RuntimeException e) {
                log.debug("При удалении лайка к фильму возникла внутренняя ошибка серввера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    // Метод отражает 10 самых популярных фильмов на основе количества лайков у каждого или заданое число фильмов
    public List<Film> displayTenTheMostPopularFilmsIsParamIsNotDefined(Long count) {
        try {
            System.out.println(count);
            long amount = 10; // Значение по умолчанию
            if (count != null) {
                amount = count;
                System.out.println(amount);
            }
            log.debug("Возвращаем список с самыми популярными фильмами");
            List<Film> films = filmStorage.getFilms();
            return films.stream()
                    .sorted((o1, o2) -> (int) (o2.getRate() - o1.getRate()))
                    .limit(amount)
                   .collect(Collectors.toList());
        } catch (RuntimeException | SQLException e) {
            throw new RuntimeException("Внутренняя ошибка сервера");
        }
    }
}
