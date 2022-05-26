package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmStorage {
    Film create(Film film) throws RuntimeException; // Метод создающий/Добавляющий фильм в хранилище
    Film update(Film film) throws RuntimeException; // Метод обновляющий фильм или если такого фильма нет,
    // создает новый фильм (Модификация)
    void delete(Film film) throws RuntimeException; // Метод удаляющий фильм
    List<Film> getFilms() throws RuntimeException; // Метод по получению всех фильмов
    Film getFilmById(Long id) throws RuntimeException; // Метод по получени одного фильма
}
