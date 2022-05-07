package ru.yandex.practicum.filmorate.controller;

import MyException.ValidationException;
import ru.yandex.practicum.filmorate.controller.model.Film;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public Film addFilm(@RequestBody Film film) throws ValidationException {
        if (film != null) { // Проверяем, что фильм не равняется пустому значению
            if (film.getName().isEmpty()) { // Если название фильма пустое, выбрасываем исключение
                log.debug("Ошибка с названием фильма");
                throw new ValidationException("Название фильма не может быть пустым");
            }
            if (film.getDescription().length() > 200) { // Если описание фильма больше, чем 200 символов
                log.debug("Ошибка с описанием фильма");
                throw new ValidationException("Описание фильма не должно быть больше, чем 200 символов");
            }
            if (film.getDescription().isEmpty()) { // Если описание фильма пустое
                log.debug("Ошибка с описанием фильма");
                throw new ValidationException("Описание фильма не должно быть пустым");
            }
            // Если дата релиза раньше чем 28 декабря 1895 года
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                log.debug("Ошибка с датой релиза фильма");
                throw new ValidationException("Релиз фильма должен быть не раньше 28 декабря 1895 года");
            }
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
