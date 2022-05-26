package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    Map<Long, Film> mapWithAllFilms = new HashMap<>();

    // Метод, который добавляет новый фильм
    @Override
    public Film create(Film film) {
        if (film == null) {
            log.debug("При попытке создать новый фильм произошла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        } else {
            log.debug("Устанавливаем автоматически ID для фильма");
            film.setId(mapWithAllFilms.size() + 1);
        }
        try {
            log.debug("Новый фильм успешно создан/добавлен");
            mapWithAllFilms.put(film.getId(), film);
            return mapWithAllFilms.get(film.getId());
        } catch (RuntimeException e) {
            log.debug("При попытке создать новый фильм произошла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    // Метод, который обновляет информацию по существующему фильму
    @Override
    public Film update(Film film) {
        if (film == null) {
            log.debug("При обновлении фильма передали значение Null");
            throw new ValidationException("Ошибка валидации");
        }
        if (film.getId() < 0 || mapWithAllFilms.get(film.getId()) == null) {
            log.debug("При обновлении фильма объект с ID - " + film.getId() + " не был найден");
            throw new NotFoundException("Искомый объект не найден");
        } else {
            try {
                log.debug("Обновляем информацию по фильму через ID - " + film.getId());
                film.setRate(film.getRate());
                mapWithAllFilms.put(film.getId(), film);
                log.debug("Пытаемся вернуть пользователя после обновления");
                return mapWithAllFilms.get(film.getId());
            } catch (RuntimeException e) {
                log.debug("При обновлении фильма возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    // Метод удаляющий фильм
    @Override
    public void delete(Film film) throws Throwable {
        if (film == null) {
            log.debug("При удаления фильма возникла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        }
        if (film.getId() < 0 || mapWithAllFilms.get(film.getId()) == null) {
            log.debug("При удалении фильма возникла ошибка с ID");
            throw new ValidationException("Ошибка валидации");
        } else if (mapWithAllFilms.containsValue(film)) {
            try {
                log.debug("Пытаемся удалить фильм");
                mapWithAllFilms.remove(film);
            } catch (RuntimeException e) {
                log.debug("При удалении фильма возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    // Метод по получению всех фильмов
    @Override
    public List<Film> getAllFilms() {
        try {
            log.debug("Пытаемся вернуть список всех фильмов");
            return new ArrayList<>(mapWithAllFilms.values());
        } catch (RuntimeException exception) {
            log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    // Метод возвращающий фильма одного по ID
    @Override
    public Film getOneFilm(Long id) throws Throwable {
        if (id < 0) {
            log.debug("При попытке вернуть фильм возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        if (mapWithAllFilms.get(id) == null) {
            log.debug("При получении фильма возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Пытаюсь вернуть один фильм");
                return mapWithAllFilms.get(id);
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть фильм возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }
}
