package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.*;


@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }


    // Метод, который добавляет новый фильм
    @PostMapping
    public Film create(@Valid @RequestBody Film film) throws Throwable {
        return filmService.getFilmStorage().create(film);
    }

    // Метод, который обновляет информацию по существующему фильму или создает и добавляет новый фильм
    @PutMapping
    public Film update(@Valid @RequestBody Film film) throws Throwable {
        return filmService.getFilmStorage().update(film);
    }

    // Метод удаляющий фильм
    @DeleteMapping
    public void delete(@Valid @RequestBody Film film) throws Throwable {
        filmService.getFilmStorage().delete(film);
    }

    // Метод по получению всех фильмов
    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getFilmStorage().getAllFilms();
    }

    // Метод по получению одного пользователя (переменная пути)
    @GetMapping("/{id}")
    public Film getOneFilm(@PathVariable Long id) throws Throwable {
        return filmService.getFilmStorage().getOneFilm(id);
    }

    // Метод (пользователь ставит лайк фильму)
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) throws Throwable {
        filmService.addLike(id, userId);
    }

    // Метод (пользователь удаляет лайк)
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) throws Throwable {
        filmService.deleteLike(id, userId);
    }

    // Метод возвращает топ 10 лучших фильмов по кол-ву лайков (по умолчанию), можно задать значение не равное 10
    @GetMapping("/popular")
    public List<Film> displayTenTheMostPopularFilmsIsParamIsNotDefined (@RequestParam (required = false) Long count) throws Throwable {
        return filmService.displayTenTheMostPopularFilmsIsParamIsNotDefined(count);
    }
}
