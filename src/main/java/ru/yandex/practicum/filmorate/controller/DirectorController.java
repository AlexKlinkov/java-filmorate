package ru.yandex.practicum.filmorate.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDirector;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.dao.DirectorsDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
public class DirectorController {

    private DirectorsDbStorage directorsDbStorage;
    private FilmService filmService;

    @Autowired
    public DirectorController(DirectorsDbStorage directorsDbStorage,FilmService filmService) {
        this.directorsDbStorage = directorsDbStorage;
        this.filmService =filmService;
    }


    @PostMapping
    public FilmDirector create(@Valid @RequestBody FilmDirector filmDirector) throws RuntimeException {
        log.debug("Создаю режиссера в контроллере");
        return directorsDbStorage.create(filmDirector);
    }

    // Метод, который обновляет информацию по существующему режиссеру
    @PutMapping
    public FilmDirector update(@Valid @RequestBody FilmDirector filmDirector) throws RuntimeException {
        return directorsDbStorage.update(filmDirector);
    }

    // Метод удаляющий режиссера
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) throws RuntimeException {
        directorsDbStorage.delete(id);
    }

    // Метод по получению всех режиссеров
    @GetMapping
    public List<FilmDirector> getAll() throws RuntimeException, SQLException {
        return directorsDbStorage.getDirectors();
    }

    // Метод по получению одного режиссера (переменная пути)
    @GetMapping("/{id}")
    public FilmDirector getOne(@PathVariable Long id) throws RuntimeException {
        return directorsDbStorage.getDirectorById(id);
    }


}

