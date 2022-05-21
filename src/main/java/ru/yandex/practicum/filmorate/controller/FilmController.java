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
    public Film create(@Valid @RequestBody Film film) throws ValidationException {
        return filmService.getFilmStorage().create(film);
    }

    // Метод, который обновляет информацию по существующему фильму или создает и добавляет новый фильм
    @PutMapping
    public void update(@Valid @RequestBody Film film) throws ValidationException {
        filmService.getFilmStorage().update(film);
    }

    // Метод удаляющий фильм
    @DeleteMapping
    public void delete(@Valid @RequestBody Film film) {
        filmService.getFilmStorage().delete(film);
    }

    // Метод по получению всех фильмов
    @GetMapping
    public Map<Long, Film> getAllFilms() {
        return filmService.getFilmStorage().getAllFilms();
    }

    // Метод по получению одного пользователя (переменная пути)
    @GetMapping("/{id}")
    public Film getOneFilm(@Valid @PathVariable Long id) {
        return filmService.getFilmStorage().getAllFilms().get(id);
    }

    // Метод (пользователь ставит лайк фильму)
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@Valid @PathVariable Long id, @Valid @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    // Метод (пользователь удаляет лайк)
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@Valid @PathVariable Long id, @Valid @PathVariable Long userId){
        filmService.deleteLike(id, userId);
    }

    // Метод возвращает топ 10 лучших фильмов по кол-ву лайков (по умолчанию), можно задать значение не равное 10
    @GetMapping("/popular")
    public List<Film> displayTenTheMostPopularFilmsIsParamIsNotDefined (@RequestParam (required = false) Long count) {
        return filmService.displayTenTheMostPopularFilmsIsParamIsNotDefined(count);
    }
}
