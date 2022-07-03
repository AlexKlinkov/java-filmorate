package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.LikeStatusDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Service
public class FilmService {

    private final FilmStorage filmStorage; // Хранилище с фильмами
    private final UserStorage userStorage; // Хранилище с пользователями
    private final JdbcTemplate jdbcTemplate; // Объект для работы с БД
    private final LikeStatusDbStorage likeStatusDbStorage;
    private final MPADbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;
    private  final LikeStatusDbStorage likeStatusDbStorage;
    private final EventDbStorage eventDbStorage;

    // Внедряем доступ сервиса к хранилищу с фильмами
    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                       @Qualifier("UserDbStorage") UserStorage userStorage, JdbcTemplate jdbcTemplate, LikeStatusDbStorage likeStatusDbStorage, EventDbStorage eventDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
        this.likeStatusDbStorage = likeStatusDbStorage;
        this.eventDbStorage = eventDbStorage;
        this.mpaDbStorage = mpaDbStorage;
        this.genreDbStorage = genreDbStorage;
    }

    // Метод по добавлению лайка
    public void addLike(Long filmId, Long userId) {
        if (filmId < 0 || userId < 0) {
            log.debug("При добавлении лайка возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        } else if (filmStorage.getFilmById(filmId) == null || userStorage.getUserById(userId) == null) {
            log.debug("При добавлении лайка возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Делаем запись в таблицу like_status");
                likeStatusDbStorage.addLike(filmId, userId);
                log.debug("Получаем фильм из БД");
                Film film = filmStorage.getFilmById(filmId);
                log.debug("Увеличиваем пользовательский рейтинг фильма на 1");
                film.setRate(film.getRate() + 1);
                log.debug("Обновляем фильм в БД при добавлении лайка");
                filmStorage.update(film);
                log.debug("Добавляем в таблицы событий добавление лайка пользователем фильму");
                eventDbStorage.addEvent(userId, filmId, "LIKE", "ADD");
            } catch (RuntimeException e) {
                log.debug("При добавлении лайка к фильму возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    // Метод по удалению лайка
    public void deleteLike(Long filmId, Long userId) {
        if (filmId < 0 || userId < 0) {
            log.debug("При удалении лайка возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        } else if (filmStorage.getFilmById(filmId) == null || userStorage.getUserById(userId) == null) {
            log.debug("При удалении лайка возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Удаляем запись из таблицы like_status если такая запись существует");
                if (likeStatusDbStorage.likeWasDetected(filmId, userId)) {
                    likeStatusDbStorage.deleteLike(filmId, userId);
                    log.debug("Получаем фильм из БД");
                    Film film = filmStorage.getFilmById(filmId);
                    log.debug("Уменьшаем пользовательский рейтинг фильма на 1");
                    film.setRate(film.getRate() - 1);
                    log.debug("Обновляем фильм в БД при удалении лайка");
                    filmStorage.update(film);
                    log.debug("Добавляем в таблицы событие удаление лайка у фильма пользователем");
                    eventDbStorage.addEvent(userId, filmId, "LIKE", "ADD");
                }
            } catch (RuntimeException e) {
                log.debug("При удалении лайка к фильму возникла внутренняя ошибка серввера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    // Метод отражает 10 самых популярных фильмов на основе количества лайков у каждого или заданое число фильмов
    public List<Film> displayTenTheMostPopularFilmsIsParamIsNotDefined(int count) {
        TreeSet<Genre> filmGenres = new TreeSet<>();
        List<Film> listToReturn = new ArrayList<>();
        if (count > 0) {
            String sqlQuery = ("SELECT film.*, COUNT(like_status.user_id) AS likes_count " +
                    "FROM film " +
                    "LEFT JOIN like_status ON film.id = like_status.film_id " +
                    "GROUP BY film.id " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT " + count);
            SqlRowSet filmRowSet = jdbcTemplate.queryForRowSet(sqlQuery);
            while (filmRowSet.next()) {
                Film film = new Film(
                        filmRowSet.getLong("ID"),
                        filmRowSet.getString("NAME"),
                        filmRowSet.getString("DESCRIPTION"),
                        filmRowSet.getLong("DURATION"),
                        filmRowSet.getDate("RELEASE_DATE").toLocalDate(),
                        mpaDbStorage.getMPAById(filmRowSet.getInt("MPA_id"))
                );
                String sqlQueryGenre = "SELECT * FROM genre WHERE id IN (SELECT genre_id FROM film_genre WHERE film_id = ?)";
                SqlRowSet genreRowSet = jdbcTemplate.queryForRowSet(sqlQueryGenre, film.getId());
                while (genreRowSet.next()) {
                    Genre genreFound = new Genre(
                            genreRowSet.getInt("id"),
                            genreRowSet.getString("name")
                    );
                    filmGenres.add(genreFound);
                }
                if (filmGenres.size() != 0) {
                    film.setGenres(filmGenres);
                } else {
                    film.setGenres(null);
                }
                listToReturn.add(film);
            }
        } else {
            String sqlQuery = ("SELECT film.*, COUNT(like_status.user_id) AS likes_count " +
                    "FROM film " +
                    "LEFT JOIN like_status ON film.id = like_status.film_id " +
                    "GROUP BY film.id " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT 10");
            SqlRowSet filmRowSet = jdbcTemplate.queryForRowSet(sqlQuery);
            while (filmRowSet.next()) {
                Film film = new Film(
                        filmRowSet.getLong("ID"),
                        filmRowSet.getString("NAME"),
                        filmRowSet.getString("DESCRIPTION"),
                        filmRowSet.getLong("DURATION"),
                        filmRowSet.getDate("RELEASE_DATE").toLocalDate(),
                        mpaDbStorage.getMPAById(filmRowSet.getInt("MPA_id"))
                );
                String sqlQueryGenre = "SELECT * FROM genre WHERE id IN (SELECT genre_id FROM film_genre WHERE film_id = ?)";
                SqlRowSet genreRowSet = jdbcTemplate.queryForRowSet(sqlQueryGenre, film.getId());
                while (genreRowSet.next()) {
                    Genre genreFound = new Genre(
                            genreRowSet.getInt("id"),
                            genreRowSet.getString("name")
                    );
                    filmGenres.add(genreFound);
                }
                if (filmGenres.size() != 0) {
                    film.setGenres(filmGenres);
                } else {
                    film.setGenres(null);
                }
                listToReturn.add(film);
            }
        }
        return listToReturn;
    }

    //Works without DB
    /*
    public List<Film> mostPopularsByGenreYear(Integer count, Integer generId, Integer year) {
        List<Film> filmList = new ArrayList<>(displayTenTheMostPopularFilmsIsParamIsNotDefined(count));
        List<Film> listToReturn = new ArrayList<>();
        for (Film f : filmList) {
            if (generId == null) {
                if (f.getReleaseDate().getYear() == year) {
                    listToReturn.add(f);
                }
            }
            if (year == null) {
                if (f.getGenres().contains(genreDbStorage.getOneById(generId))) {
                    listToReturn.add(f);
                }
            }
            if (generId != null && year != null) {
                if (f.getReleaseDate().getYear() == year && f.getGenres().contains(genreDbStorage.getOneById(generId))) {
                    listToReturn.add(f);
                }
            }
        }
        if (count == 0) {
            return listToReturn.stream().limit(1).collect(Collectors.toList());
        }

        return listToReturn;
    }*/

    //Works with DB
    public List<Film> mostPopularsByGenreYear(int count, Integer generId, Integer year) {
        TreeSet<Genre> filmGenres = new TreeSet<>();
        List<Film> listToReturn = new ArrayList<>();
        if (count == 0) {
            count = 10;
        }
        if (generId == null) {
            String sqlQuery = ("SELECT film.*, COUNT(like_status.user_id) AS likes_count " +
                    "FROM film " +
                    "LEFT JOIN like_status ON film.id = like_status.film_id " +
                    "WHERE EXTRACT (year from film.release_date) = " + year +
                    " GROUP BY film.id " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT " + count);
            SqlRowSet filmRowSet = jdbcTemplate.queryForRowSet(sqlQuery);
            while (filmRowSet.next()) {
                Film film = new Film(
                        filmRowSet.getLong("ID"),
                        filmRowSet.getString("NAME"),
                        filmRowSet.getString("DESCRIPTION"),
                        filmRowSet.getLong("DURATION"),
                        filmRowSet.getDate("RELEASE_DATE").toLocalDate(),
                        mpaDbStorage.getMPAById(filmRowSet.getInt("MPA_id"))
                );
                String sqlQueryGenre = "SELECT * FROM genre WHERE id IN (SELECT genre_id FROM film_genre WHERE film_id = ?)";
                SqlRowSet genreRowSet = jdbcTemplate.queryForRowSet(sqlQueryGenre, film.getId());
                while (genreRowSet.next()) {
                    Genre genreFound = new Genre(
                            genreRowSet.getInt("id"),
                            genreRowSet.getString("name")
                    );
                    filmGenres.add(genreFound);
                }
                if (filmGenres.size() != 0) {
                    film.setGenres(filmGenres);
                } else {
                    film.setGenres(null);
                }
                listToReturn.add(film);
            }
            return listToReturn;
        }

        if (year == null) {
            String sqlQuery = ("SELECT film.*, COUNT(like_status.user_id) AS likes_count " +
                    "FROM film " +
                    "LEFT JOIN like_status ON film.id = like_status.film_id " +
                    "LEFT JOIN film_genre ON film.id = film_genre.film_id " +
                    "WHERE film_genre.genre_id = " + generId +
                    " GROUP BY film.id " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT " + count);
            SqlRowSet filmRowSet = jdbcTemplate.queryForRowSet(sqlQuery);
            while (filmRowSet.next()) {
                Film film = new Film(
                        filmRowSet.getLong("ID"),
                        filmRowSet.getString("NAME"),
                        filmRowSet.getString("DESCRIPTION"),
                        filmRowSet.getLong("DURATION"),
                        filmRowSet.getDate("RELEASE_DATE").toLocalDate(),
                        mpaDbStorage.getMPAById(filmRowSet.getInt("MPA_id"))
                );
                String sqlQueryGenre = "SELECT * FROM genre WHERE id IN (SELECT genre_id FROM film_genre WHERE film_id = ?)";
                SqlRowSet genreRowSet = jdbcTemplate.queryForRowSet(sqlQueryGenre, film.getId());
                while (genreRowSet.next()) {
                    Genre genreFound = new Genre(
                            genreRowSet.getInt("id"),
                            genreRowSet.getString("name")
                    );
                    filmGenres.add(genreFound);
                }
                if (filmGenres.size() != 0) {
                    film.setGenres(filmGenres);
                } else {
                    film.setGenres(null);
                }
                listToReturn.add(film);
            }
            return listToReturn;
        }

        String sqlQuery = ("SELECT film.*, COUNT(like_status.user_id) AS likes_count " +
                "FROM film " +
                "LEFT JOIN like_status ON film.id = like_status.film_id " +
                "LEFT JOIN film_genre ON film.id = film_genre.film_id " +
                "WHERE EXTRACT (year from film.release_date) = " + year +
                " AND film_genre.genre_id = " + generId +
                " GROUP BY film.id " +
                "ORDER BY likes_count DESC " +
                "LIMIT " + count);
        SqlRowSet filmRowSet = jdbcTemplate.queryForRowSet(sqlQuery);
        while (filmRowSet.next()) {
            Film film = new Film(
                    filmRowSet.getLong("ID"),
                    filmRowSet.getString("NAME"),
                    filmRowSet.getString("DESCRIPTION"),
                    filmRowSet.getLong("DURATION"),
                    filmRowSet.getDate("RELEASE_DATE").toLocalDate(),
                    mpaDbStorage.getMPAById(filmRowSet.getInt("MPA_id"))
            );
            String sqlQueryGenre = "SELECT * FROM genre WHERE id IN (SELECT genre_id FROM film_genre WHERE film_id = ?)";
            SqlRowSet genreRowSet = jdbcTemplate.queryForRowSet(sqlQueryGenre, film.getId());
            while (genreRowSet.next()) {
                Genre genreFound = new Genre(
                        genreRowSet.getInt("id"),
                        genreRowSet.getString("name")
                );
                filmGenres.add(genreFound);
            }
            if (filmGenres.size() != 0) {
                film.setGenres(filmGenres);
            } else {
                film.setGenres(null);
            }
            listToReturn.add(film);
        }
        return listToReturn;
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Не найден пользователь с идентификатором: " + user.getId());
        }
        User friend = userStorage.getUserById(friendId);
        if (friend == null) {
            throw new NotFoundException("Не найден пользователь с идентификатором: " + friend.getId());
        }
        return filmStorage.getCommonFilms(userId, friendId)
                .stream()
                .collect(Collectors.toList());

    }

    public List<Film> FilmsOfOneDirector(Long directorId) {
        try {
            log.debug("Возвращаем список с самыми популярными фильмами");
            List<Film> films = filmStorage.getFilmsOfOneDirector(directorId);
            return films.stream()
                    .sorted((o1, o2) -> (int) (o2.getRate() - o1.getRate()))
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Внутренняя ошибка сервера");
        }
    }



}
