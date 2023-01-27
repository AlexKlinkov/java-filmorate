package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.MPADbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final MPADbStorage mpaDbStorage;

    @Autowired
    public MpaController(MPADbStorage mpaDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
    }

    // Метод по получению всех возрастных категорий фильма
    @GetMapping
    public List<MPA> getAll() {
        return mpaDbStorage.getMPAs();
    }

    // Метод по получению одной категории (переменная пути)
    @GetMapping("/{id}")
    public MPA getOne(@PathVariable int id) throws RuntimeException {
        return mpaDbStorage.getMPAById(id);
    }
}
