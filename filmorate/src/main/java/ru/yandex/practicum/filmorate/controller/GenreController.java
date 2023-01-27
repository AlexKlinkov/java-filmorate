package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {

    private final GenreDbStorage genreDbStorage;

    @Autowired
    public GenreController(GenreDbStorage genreDbStorage) {
        this.genreDbStorage = genreDbStorage;
    }

    // Метод по получению всех жанров
    @GetMapping
    public List<Genre> getAll() {
        return genreDbStorage.getGenres();
    }

    // Метод по получению одного жанра (переменная пути)
    @GetMapping("/{id}")
    public Genre getOne(@PathVariable int id) throws RuntimeException {
        return genreDbStorage.getOneById(id);
    }
}
