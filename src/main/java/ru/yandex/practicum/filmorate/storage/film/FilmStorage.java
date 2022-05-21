package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Map;

public interface FilmStorage {
    Film create(Film film) throws ValidationException; // Метод создающий/Добавляющий фильм в хранилище
    void update(Film film) throws ValidationException; // Метод обновляющий фильм или если такого фильма нет, создает новый фильм (Модификация)
    void delete(Film film); // Метод удаляющий фильм
    Map<Long, Film> getAllFilms(); // Метод по получению всех фильмов
}
