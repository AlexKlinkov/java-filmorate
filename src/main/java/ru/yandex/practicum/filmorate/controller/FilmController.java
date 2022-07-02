package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.model.Film;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.*;

@Slf4j
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
    public Film create(@Valid @RequestBody Film film) throws RuntimeException {
        log.debug("Создаю фильм в контроллере");
        return filmService.getFilmStorage().create(film);
    }

    // Метод, который обновляет информацию по существующему фильму или создает и добавляет новый фильм
    @PutMapping
    public Film update(@Valid @RequestBody Film film) throws RuntimeException {
        return filmService.getFilmStorage().update(film);
    }

    // Метод удаляющий фильм
    @DeleteMapping
    public void delete(@Valid @RequestBody Film film) throws RuntimeException {
        filmService.getFilmStorage().delete(film);
    }

    // Метод по получению всех фильмов
    @GetMapping
    public List<Film> getAll() throws RuntimeException, SQLException {
        return filmService.getFilmStorage().getFilms();
    }

    // Метод по получению одного пользователя (переменная пути)
    @GetMapping("/{id}")
    public Film getOne(@PathVariable Long id) throws RuntimeException {
        return filmService.getFilmStorage().getFilmById(id);
    }


    // Метод (получение фильмов по режиссеру)
    @GetMapping("director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable long directorId,
                                         @RequestParam String sortBy) {
        if (sortBy.equals("year")) {
            return filmService.getFilmStorage().getFilmsByDirectorSortedByYear(directorId);
        } else if (sortBy.equals("likes")){
            return filmService.getFilmStorage().FilmsOfOneDirector(directorId);
        }
        return null;
    }

    // Метод (поиска по названиям и режиссерам)
    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam String query,
                                  @RequestParam String by){
        return filmService.getFilmStorage().searchFilms(query,by);
    }

    // Метод (пользователь ставит лайк фильму)
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) throws RuntimeException {
        filmService.addLike(id, userId);
    }

    // Метод (пользователь удаляет лайк)
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) throws RuntimeException {
        filmService.deleteLike(id, userId);
    }

    // Метод возвращает топ 10 лучших фильмов по кол-ву лайков (по умолчанию), можно задать значение не равное 10
    @GetMapping("/popular")
    public List<Film> displayTenTheMostPopularFilmsIsParamIsNotDefined
    (@RequestParam (required = false) Long count) throws RuntimeException {
        return filmService.displayTenTheMostPopularFilmsIsParamIsNotDefined(count);
    }
}
