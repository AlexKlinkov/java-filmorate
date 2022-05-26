package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Service
public class FilmService {

    private final FilmStorage filmStorage; // Хранилище с фильмами
    private final UserStorage userStorage; // Хранилище с пользователями

    // Внедряем доступ сервиса к хранилищу с фильмами
    @Autowired
    public FilmService(@Qualifier("InMemoryFilmStorage") FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
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
                log.debug("Достаем фильм из хранилища при добавлении лайка");
                Film film = filmStorage.getFilmById(filmId);
                log.debug("Обновляем фильм для хранилища при добавлении лайка удаляем предыдущую версию фильма");
                filmStorage.getFilms().remove(film);
                log.debug("Добавляем ко множеству с лайками фильма новый лайк, как ID пользователя лайкнувшего фильм" +
                        "(один пользователь, один лайк)");
                film.getSetWithLike().add(userId);
                film.setRate((long)film.getSetWithLike().size());
                log.debug("Обновляем фильм для хранилища при добавлении лайка, добавляем обновленный фильм в мапу");
                filmStorage.getFilms().add(film);
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
                log.debug("Достаем фильм из хранилища");
                Film film = filmStorage.getFilmById(filmId);
                log.debug("Обновлем информацию в хранилище удаляя предыдущий фильм");
                filmStorage.getFilms().remove(film);
                log.debug("Удаляем из множества лайк к фильму");
                film.getSetWithLike().remove(userId);
                film.setRate((long)film.getSetWithLike().size());
                log.debug("Обновляем фильм для хранилища добавляя тот же фильм без лайка");
                filmStorage.getFilms().add(film);
            } catch (RuntimeException e) {
                log.debug("При удалении лайка к фильму возникла внутренняя ошибка серввера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    // Метод отражает 10 самых популярных фильмов на основе количества лайков у каждого или заданое число фильмов
    public List<Film> displayTenTheMostPopularFilmsIsParamIsNotDefined(Long count) {
        try {
            long amount = 10; // Значение по умолчанию
            if (count != null) {
                amount = count;
            }
            log.debug("Возвращаем список с самыми популярными фильмами");
            return filmStorage.getFilms().stream()
                    .sorted((o1, o2) -> o2.getSetWithLike().size() - o1.getSetWithLike().size())
                    .limit(amount)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Внутренняя ошибка сервера");
        }
    }
}
