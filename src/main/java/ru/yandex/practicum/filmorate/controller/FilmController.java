package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.MyException.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    Comparator<Integer> comparatorID = new Comparator<>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2; // если отрицательное число, то первый объект меньше;
        }
    };
    Map<Integer, Film> mapWithAllFilms = new TreeMap<>(comparatorID); // Мапа со всеми фильмами отсортированными по ID
    // от меньшего к большему

    // Метод, который добавляет новый фильм
    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) throws ValidationException {
        if (film != null) { // Проверяем, что фильм не равняется пустому значению
            // Если продолжительность фильма не положительная
            if (film.getDuration().isNegative() || film.getDuration().isZero()) {
                log.debug("Ошибка с продолжительностью фильма");
                throw new ValidationException("Продолжительность фильма должна быть больше 0");
            }
            log.debug("Фильм успешно добавлен");
            mapWithAllFilms.put(film.getId(), film);
            return mapWithAllFilms.get(film.getId());
        } else { // Если фильм пустое значение выбрасываем ошибку 400
            throw new ValidationException(HttpStatus.BAD_REQUEST.toString());
        }
    }

    // Метод, который обновляет информацию по существующему фильму или создает и добавляет новый фильм
    @PutMapping
    public void updateFilm(@RequestBody Film film) throws ValidationException {
        if (film != null) { // Проверяем, что фильм не равняется пустому значению
            // Обновляем существующий фильм
            // Так как у нас все фильмы имеют ID в мапе в качестве ключа, то алгорит будет не O(n), как при структуре
            // данных СПИСОК ИЛИ МНОЖЕСТВО, где пришлось бы делать циклы, а O(1)
            if (mapWithAllFilms.containsKey(film.getId())) { // Если фильм с таким ID уже существует
                int id = film.getId(); // ID существующего фильма
                // Для существующего фильма обновляем название
                log.debug("Название фильма успешно обновлено");
                mapWithAllFilms.get(id).setName(film.getName());
                // Для существующего фильма обновляем описание
                log.debug("Описание фильма успешно обновлено");
                mapWithAllFilms.get(id).setDescription(film.getDescription());
                // Для существующего фильма обновляем дату релиза
                log.debug("Дата релиза фильма успешно обновлена");
                mapWithAllFilms.get(id).setReleaseDate(film.getReleaseDate());
                // Для существующего фильма обновляем продолжительность
                log.debug("Продолжительность фильма успешно обновлена");
                mapWithAllFilms.get(id).setDuration(film.getDuration());
                return;
                // Если фильма не существует, создаем новый
            } else {
                log.debug("Фильм успешно добавлен");
                addFilm(film);
            }
        } else { // Если фильм пустое значение выбрасываем ошибку 400
            throw new ValidationException(HttpStatus.BAD_REQUEST.toString());
        }
    }

    // Метод по получению всех фильмов
    @GetMapping
    public Map<Integer, Film> getAllFilms() {
        log.debug("Клиент получил перечень всех фильмов");
        return mapWithAllFilms;
    }
}
