package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    Map<Long, Film> mapWithAllFilms = new HashMap<>(); // Мапа со всеми фильмами

    // Метод, который добавляет новый фильм
    @Override
    public Film create(Film film) throws ValidationException {
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
    @Override
    public void update(Film film) throws ValidationException {
        if (film != null) { // Проверяем, что фильм не равняется пустому значению
            // Обновляем существующий фильм
            // Так как у нас все фильмы имеют ID в мапе в качестве ключа, то алгорит будет не O(n), как при структуре
            // данных СПИСОК ИЛИ МНОЖЕСТВО, где пришлось бы делать циклы, а O(1)
            if (mapWithAllFilms.containsKey(film.getId())) { // Если фильм с таким ID уже существует
                Film filmFromMap = mapWithAllFilms.get(film.getId()); // Существующий фильм, взяли из мапы
                // Для существующего фильма обновляем название
                log.debug("Название фильма успешно обновлено");
                filmFromMap.setName(film.getName());
                // Для существующего фильма обновляем описание
                log.debug("Описание фильма успешно обновлено");
                filmFromMap.setDescription(film.getDescription());
                // Для существующего фильма обновляем дату релиза
                log.debug("Дата релиза фильма успешно обновлена");
                filmFromMap.setReleaseDate(film.getReleaseDate());
                // Для существующего фильма обновляем продолжительность
                log.debug("Продолжительность фильма успешно обновлена");
                filmFromMap.setDuration(film.getDuration());
                return;
                // Если фильма не существует, создаем новый
            } else {
                log.debug("Фильм успешно добавлен");
                create(film);
            }
        } else { // Если фильм пустое значение выбрасываем ошибку 400
            throw new ValidationException(HttpStatus.BAD_REQUEST.toString());
        }
    }

    // Метод удаляющий фильм
    @Override
    public void delete(Film film) {
        mapWithAllFilms.remove(film.getId());
    }

    // Метод по получению всех фильмов
    @Override
    public Map<Long, Film> getAllFilms() {
        return mapWithAllFilms;
    }
}
