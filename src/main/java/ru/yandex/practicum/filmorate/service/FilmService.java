package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;

import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Service
public class FilmService {

    private final FilmStorage filmStorage; // Хранилище с фильмами

    // Внедряем доступ сервиса к хранилищу с фильмами
    @Autowired
    public FilmService(@Qualifier("InMemoryFilmStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    // Метод по добавлению лайка
    public void addLike(Long filmId, Long userId) {
        // Достаем фильм из хранилища
        Film film = filmStorage.getAllFilms().get(filmId);
        // Добавляем ко множеству с лайками фильма новый лайк, как ID пользователя лайкнувшего фильм
        // (один пользователь, один лайк)
        film.getSetWithLike().add(userId);
        // Обновляем фильм для хранилища
        filmStorage.getAllFilms().put(filmId, film);
    }

    // Метод по удалению лайка
    public void deleteLike(Long filmId, Long userId) {
        // Достаем фильм из хранилища
        Film film = filmStorage.getAllFilms().get(filmId);
        // Удаляем из множества лайк к фильму
        film.getSetWithLike().remove(userId);
        // Обновляем фильм для хранилища
        filmStorage.getAllFilms().put(filmId, film);
    }

    // Метод отражает 10 самых популярных фильмов на основе количества лайков у каждого или заданое число фильмов
    public List<Film> displayTenTheMostPopularFilmsIsParamIsNotDefined(Long count){
        long amount = 10; // Значение по умолчанию
        if (count != null) {
            amount = count;
        }
        Map<Long, Film> films = new HashMap<>(filmStorage.getAllFilms()); // Мапа со всеми фильмами
        Map<Long, Long> amountOfLikes = new HashMap<>(); // Мапа с ID фильмами (ключ) и количеством лайков (значение)
        for (Film film : films.values()) {
            amountOfLikes.put(film.getId(), (long) (film.getSetWithLike().size()));
        }
        // Сортируем мапу (фильм с наибольшем количеством лайков в мапе первый)
        Map<Long, Long> sortedMap = amountOfLikes.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(amount)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        // Добавляем первые 10 фильмов по популярности в список для возврата его пользователю
        List<Film> listWithTheMostPopularFilms = new ArrayList<>();
        for (Long filmId : sortedMap.values()) {
            listWithTheMostPopularFilms.add(filmStorage.getAllFilms().get(filmId));
        }
        return listWithTheMostPopularFilms;
    }
}
