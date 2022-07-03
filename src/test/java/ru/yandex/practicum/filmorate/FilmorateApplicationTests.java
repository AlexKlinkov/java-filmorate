package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationExceptionFilmorate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
	private final UserDbStorage userStorage;
	private final FilmDbStorage filmStorage;
	private User user = new User("1kot@mail.ru", "KotoMax",
				"Vasia", LocalDate.of(1193, 03,25));
	private Film film = new Film(0,"Spider-Man", "Young man...",
			100L,LocalDate.of(2003, 03, 25),
			new MPA(1, "G"), Set.of(new Genre(1, "Комедия")), 2);

	                                            // ТЕСТИМ ХРАНИЛИЩЕ С ПОЛЬЗОВАТЕЛЯМИ
	@Test
	public void testCreateUser() {
		userStorage.create(user);
		Assertions.assertNotNull(userStorage.getUserById(1));
	}

	@Test
	public void testUpdateUser() {
		userStorage.create(user);
		User newUser = new User(1, "1kot@mail.ru", "NEWKOTOMAX",
				"Vasia", LocalDate.of(1993, 03, 25));
		userStorage.update(newUser);
		Assertions.assertEquals("NEWKOTOMAX", userStorage.getUserById(1).getLogin());
	}
	@Test
	public void testDeleteUser() {
		User user2 = userStorage.create(user);
		userStorage.delete(user2);
		ValidationExceptionFilmorate exception = Assertions.assertThrows(ValidationExceptionFilmorate.class,
				() -> userStorage.getUserById(user.getId()));
		Assertions.assertEquals("Ошибка валидации",
				exception.getMessage());
	}
	@Test
	public void testGetUsers() {
		userStorage.create(user);
		List<User> users = new ArrayList<>();
		users.addAll(userStorage.getUsers());
		Assertions.assertEquals(1, users.size());
	}

	@Test
	public void testFindUserById() {
		userStorage.create(user);
		Assertions.assertNotNull(userStorage.getUserById(1));
	}

												// ТЕСТИМ ХРАНИЛИЩЕ С ФИЛЬМАМИ
	@Test
	public void testCreateFilm() {
		filmStorage.create(film);
		Assertions.assertNotNull(filmStorage.getFilmById(1));
	}

	@Test
	public void testUpdateFilm() {
		filmStorage.create(film);
		Film newFilm = new Film(1,"Wonderful Spider-Man", "Young man...", 100L,
				LocalDate.of(2003, 03, 25),
				new MPA(1, "G"), Set.of(new Genre(1, "Комедия")), 3);
		filmStorage.update(newFilm);
		Assertions.assertEquals("Wonderful Spider-Man", filmStorage.getFilmById(1).getName());
	}
	@Test
	public void testDeleteFilm() {
		Film film2 = filmStorage.create(film);
		filmStorage.delete(film2);
		ValidationExceptionFilmorate exception = Assertions.assertThrows(ValidationExceptionFilmorate.class,
				() -> filmStorage.getFilmById(film.getId()));
		Assertions.assertEquals("Ошибка валидации",
				exception.getMessage());
	}
	@Test
	public void testGetFilms() {
		filmStorage.create(film);
		List<Film> users = new ArrayList<>();
		users.addAll(filmStorage.getFilms());
		Assertions.assertEquals(1, users.size());
	}

	@Test
	public void testFindFilmById() {
		filmStorage.create(film);
		Assertions.assertNotNull(filmStorage.getFilmById(1));
	}
}
