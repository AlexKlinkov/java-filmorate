package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmStorage {
    Film create(Film film) throws Throwable; // Метод создающий/Добавляющий фильм в хранилище
    Film update(Film film) throws Throwable; // Метод обновляющий фильм или если такого фильма нет, создает новый фильм (Модификация)
    void delete(Film film) throws Throwable; // Метод удаляющий фильм
    List<Film> getAllFilms(); // Метод по получению всех фильмов
    Film getOneFilm(Long id) throws Throwable; // Метод по получени одного фильма
}
