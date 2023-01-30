package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest
class FilmDBStorageTest {
    private final FilmDbStorage filmStorage;
    private Film film = new Film(0L, "Spider-Man", "Young man...",
            100L, LocalDate.of(2003, 03, 25),
            new MPA(1, "G"), Set.of(new Genre(1, "Комедия")), 2, null);

    @Autowired
    FilmDBStorageTest(FilmDbStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void init() {
        filmStorage.create(film);
    }

    @AfterEach
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void del() {
        filmStorage.deleteById(film.getId());
    }

    // ТЕСТИМ ХРАНИЛИЩЕ С ФИЛЬМАМИ

    @Test
    public void testUpdateFilm() throws SQLException {
        Film newFilm = new Film(film.getId(), "Wonderful Spider-Man", "Young man...", 100L,
                LocalDate.of(2003, 3, 25),
                new MPA(1, "G"), Set.of(new Genre(1, "Комедия")), 3, null);
        filmStorage.update(newFilm);
        Assertions.assertEquals("Wonderful Spider-Man", filmStorage.getFilmById(newFilm.getId()).getName());
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void testGetFilms() {
        List<Film> users = new ArrayList<>(filmStorage.getFilms());
        Assertions.assertEquals(1, users.size());
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void testFindFilmById() {
        Assertions.assertNotNull(filmStorage.getFilmById(film.getId()));
    }
}
