package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film create(Film film) throws RuntimeException; // Метод создающий/Добавляющий фильм в хранилище
    Film update(Film film) throws RuntimeException; // Метод обновляющий фильм или если такого фильма нет,
    // создает новый фильм (Модификация)
    void delete(Film film) throws RuntimeException; // Метод удаляющий фильм
    List<Film> getFilms() throws RuntimeException, SQLException; // Метод по получению всех фильмов
    Film getFilmById(long id) throws RuntimeException; // Метод по получени одного фильма

    Collection<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getFilmsOfOneDirector(Long directorId);

    List<Film> getFilmsByDirectorSortedByYear(Long directorId);

    List<Film> FilmsOfOneDirector(Long directorId);

    List<Film> searchFilms(String query, String by);
}
