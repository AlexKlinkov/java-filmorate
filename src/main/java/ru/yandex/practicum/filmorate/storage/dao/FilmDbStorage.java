package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDirector;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmsGenresDbStorage filmsGenresDbStorage;
    private final MPADbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final LikeStatusDbStorage likeStatusDbStorage;
    private final DirectorsDbStorage directorsDbStorage;
    private final FilmDirectorsDBStorage filmDirectorsDBStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmsGenresDbStorage filmsGenresDbStorage, MPADbStorage mpaDbStorage, GenreDbStorage genreDbStorage, LikeStatusDbStorage likeStatusDbStorage, DirectorsDbStorage directorsDbStorage, FilmDirectorsDBStorage filmDirectorsDBStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmsGenresDbStorage = filmsGenresDbStorage;
        this.mpaDbStorage = mpaDbStorage;
        this.genreDbStorage = genreDbStorage;
        this.likeStatusDbStorage = likeStatusDbStorage;
        this.directorsDbStorage = directorsDbStorage;
        this.filmDirectorsDBStorage = filmDirectorsDBStorage;
    }

    @Override
    public Film create(Film film) throws RuntimeException {
        if (film == null) {
            log.debug("При попытке создать новый фильм произошла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        }
        log.debug("При создании фильма проверяем, что данного фильма еще нет в БД");
        SqlRowSet alreadyExist = jdbcTemplate.queryForRowSet("select * from FILM where NAME = ? " +
                        "AND DESCRIPTION = ? AND RELEASE_DATE = ?",
                film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()));
        if (alreadyExist.first()) {
            log.debug("Если фильм уже есть в БД, то не создаем его, а возвращаем из БД, " +
                    "обеспечивая уникальность данных");
            alreadyExist.beforeFirst();
            while (alreadyExist.next()) {
                Film returnFilm = getFilmById(alreadyExist.getLong("ID"));
                if (returnFilm.getGenres() == null || returnFilm.getGenres().isEmpty()) {
                    returnFilm.setGenres(null);
                }
                return returnFilm;
            }
        }
        try {
            log.debug("Возвращаем и добавляем фильм в БД");
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("FILM").usingGeneratedKeyColumns("ID");
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("NAME", film.getName())
                    .addValue("DESCRIPTION", film.getDescription())
                    .addValue("DURATION", film.getDuration())
                    .addValue("RELEASE_DATE", Date.valueOf(film.getReleaseDate()))
                    .addValue("RATE", film.getRate())
                    .addValue("MPA_ID", film.getMpa().getId());
            Number num = jdbcInsert.executeAndReturnKey(parameters);
            film.setId(num.intValue());
            log.debug("При создании фильма запоняем возрастного рейтинга по ID");
            MPA mpa = mpaDbStorage.getMPAById(film.getMpa().getId());
            film.setMpa(mpa);
            if (film.getGenres() != null && !(film.getGenres().isEmpty())) {
                List<Genre> genres = film.getGenres().stream()
                        .sorted(Comparator.comparing(Genre::getId))
                        .collect(Collectors.toList());
                film.setGenres(new LinkedHashSet<>(genres));
                for (Genre genre : genres) {
                    log.debug("Заполняем таблицу Film_genre при создании объекта");
                    filmsGenresDbStorage.addFilmAndGenre(film.getId(), genre.getId());
                }
            } else {
                film.setGenres(null);
            }
            log.debug("При создании фильма добавляем в таблицу фильм и Id режиссера");
            if (film.getDirectors() != null && !(film.getDirectors().isEmpty())) {
                Set<FilmDirector> filmDirectors = film.getDirectors();
                for (FilmDirector director : filmDirectors) {
                    log.debug("Заполняем таблицу films_of_director при создании объекта");
                    filmDirectorsDBStorage.addFilmAndDirector(film.getId(), director.getId());
                }
            } else {
                film.setDirectors(null);
            }
            return film;
        } catch (RuntimeException e) {
            log.debug("При попытке создать новый фильм произошла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    @Override
    public Film update(Film film) throws RuntimeException {
        if (film == null) {
            log.debug("При обновлении фильма передали значение Null");
            throw new ValidationException("Ошибка валидации");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from FILM where ID = ?", film.getId());
        if (!sqlRowSet.first()) {
            log.debug("При обновлении фильма объект с ID - " + film.getId() + " не был найден");
            throw new NotFoundException("Искомый объект не найден");
        } else {
            try {
                String sqlQuery = "UPDATE FILM SET " +
                        "NAME = ?, DESCRIPTION = ?, DURATION = ?, RELEASE_DATE = ?, RATE = ?, MPA_ID = ?" +
                        " where  ID = ?";
                jdbcTemplate.update(sqlQuery,
                        film.getName(), film.getDescription(), film.getDuration(),
                        Date.valueOf(film.getReleaseDate()), film.getRate(), film.getMpa().getId(), film.getId());
                log.debug("Обновляем данные в таблице film_genre");
                if (film.getGenres() != null && !(film.getGenres().isEmpty())) {
                    log.debug("Удаляем предыдущие данные из таблицы FILM_GENRE при обновление фильма");
                    filmsGenresDbStorage.deleteFilmAndGenreByFilmId(film.getId());
                    log.debug("Вставляем новые данные в таблицу FILM_GENRE при обновление фильма");
                    Set<Genre> genres = new HashSet<>(film.getGenres());
                    for (Genre genre : genres) {
                        filmsGenresDbStorage.addFilmAndGenre(film.getId(), genre.getId());
                    }
                    log.debug("заполняем поле жанров фильма при обновлении фильма");
                    film.setGenres(genreDbStorage.getGenresByFilmId(film.getId()));
                } else {
                    log.debug("Удаляем предыдущие данные из таблицы FILM_GENRE если поле с жанрами " +
                            "оказалось пустым при обновление фильма");
                    filmsGenresDbStorage.deleteFilmAndGenreByFilmId(film.getId());
                    if (film.getGenres() != null) {
                        film.setGenres(new TreeSet<>());
                    } else {
                        film.setGenres(null);
                    }
                }
                log.debug("Обновляем данные в таблице films_of_director");
                if (film.getDirectors() != null && !(film.getDirectors().isEmpty())) {
                    log.debug("Удаляем предыдущие данные из таблицы films_of_director при обновление фильма");
                    filmDirectorsDBStorage.deleteFilmAndDirectorByFilmId(film.getId());
                    log.debug("Вставляем новые данные в таблицу films_of_director при обновление фильма");
                    Set<FilmDirector> directors = new HashSet<>(film.getDirectors());
                    for (FilmDirector director : directors) {
                        filmDirectorsDBStorage.addFilmAndDirector(film.getId(), director.getId());
                    }
                    log.debug("заполняем поле режиссеры фильма при обновлении фильма");
                    film.setDirectors(filmDirectorsDBStorage.getDirectorsByFilmId(film.getId()));
                } else {
                    log.debug("Удаляем предыдущие данные из таблицы films_of_director если поле с директором " +
                            "оказалось пустым при обновление фильма");
                    filmDirectorsDBStorage.deleteFilmAndDirectorByFilmId(film.getId());
                }
                log.debug("Заполняем поле возрастного рейтинга (имя) при обновлении фильма");
                film.setMpa(mpaDbStorage.getMPAById(film.getMpa().getId()));
                return film;
            } catch (RuntimeException e) {
                log.debug("При обновлении фильма возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    @Override
    public void delete(Film film) throws RuntimeException {
        if (film == null) {
            log.debug("При удаления фильма возникла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from FILM where ID = ?", film.getId());
        if (!sqlRowSet.first()) {
            log.debug("При удалении фильма возникла ошибка с ID");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Удалили фильм");
                filmsGenresDbStorage.deleteFilmAndGenreByFilmId(film.getId());
                likeStatusDbStorage.deleteLikeByFilmId(film.getId());
                jdbcTemplate.update("delete from FILM where ID = ?", film.getId());
            } catch (RuntimeException e) {
                log.debug("При удалении фильма возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    @Override
    public void deleteById(long id) {
        if (id < 0) {
            log.debug("При попытке удалить фильм возникла ошибка с ID: {}", id);
            throw new NotFoundException("Искомый объект не может быть найден");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from FILM where ID = ?", id);
        if (!sqlRowSet.first()) {
            log.debug("При удалении фильма возникла ошибка с ID: {}", id);
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Удалили фильм");
                filmsGenresDbStorage.deleteFilmAndGenreByFilmId(id);
                likeStatusDbStorage.deleteLikeByFilmId(id);
                jdbcTemplate.update("delete from FILM where ID = ?", id);
            } catch (RuntimeException e) {
                log.debug("При удалении фильма возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    @Override
    public List<Film> getFilms() throws RuntimeException {
        try {
            log.debug("Возвращаем список со всеми фильмами");
            String sqlQuery = "SELECT * from  FILM " +
                    "LEFT JOIN MPA ON FILM.MPA_ID = MPA.ID";
            return jdbcTemplate.query(sqlQuery, this::makeFilm);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    @Override
    public Film getFilmById(long id) throws RuntimeException {
        if (id < 0) {
            log.debug("При попытке вернуть фильм возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from FILM where ID = ?", id);
        if (!filmRows.first()) {
            log.debug("При получении фильма возникла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        } else {
            try {
                List<Film> films = getFilms();
                for (Film film : films) {
                    if (film.getId() == id) {
                        Film returnFilm = getFilms().get(new ArrayList<>(films).indexOf(film));
                        if (returnFilm.getGenres().isEmpty()) {
                            returnFilm.setGenres(null);
                        }
                        return returnFilm;
                    }
                }
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть фильм возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
        return null;
    }

    public List<Film> getFilmsOfOneDirector(Long directorId) {
        directorsDbStorage.validateDirector(directorId);
        try {
            log.debug("Возвращаем список фильмов одного режиссера");
            String sqlQuery = "SELECT FILM.*, MPA.* FROM (SELECT film_id FROM films_of_director WHERE directors_id=" + directorId
                    + ") A LEFT JOIN FILM ON FILM.ID=A.FILM_ID LEFT JOIN MPA ON FILM.MPA_ID = MPA.ID";
            return jdbcTemplate.query(sqlQuery, this::makeFilm);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми фильмами режиссера возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    public List<Film> getFilmsByDirectorSortedByYear(Long directorId) {
        directorsDbStorage.validateDirector(directorId);
        try {
            log.debug("Возвращаем отсортированный список по году одного режиссера");
            String sqlQuery = String.format("SELECT FILM.*, MPA.* FROM (SELECT film_id FROM films_of_director " +
                    "WHERE directors_id= %s) A LEFT JOIN FILM ON FILM.ID=A.FILM_ID LEFT JOIN MPA ON FILM.MPA_ID = MPA.ID" +
                    "                    ORDER BY FILM.RELEASE_DATE ASC", directorId);
            List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm);
            return returnFilmWithCorrectGenre(films);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }


    @Override
    public List<Film> FilmsOfOneDirector(Long directorId) {
        directorsDbStorage.validateDirector(directorId);
        try {
            log.debug("Возвращаем поиск по ID режиссера");
            String sqlQuery = "SELECT FILM.*, MPA.* FROM (SELECT film_id FROM films_of_director WHERE directors_id=" +
                    directorId + ") A LEFT JOIN FILM ON FILM.ID=A.FILM_ID LEFT JOIN MPA ON FILM.MPA_ID = MPA.ID";
            return jdbcTemplate.query(sqlQuery, this::makeFilm);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    public List<Film> searchFilms(String query, String by) {
        if (by.equals("director")) {
            try {
                log.debug("Возвращаем поиск по имени режиссера с сортировкой по лайкам");
                String sqlQuery = "SELECT FILM.*, MPA.* FROM (SELECT * FROM DIRECTORS WHERE UPPER(NAME) LIKE UPPER('%"
                        + query + "%')) D LEFT JOIN FILMS_OF_DIRECTOR ON D.ID=FILMS_OF_DIRECTOR.DIRECTORS_ID " +
                        "LEFT JOIN FILM ON FILM.ID=FILMS_OF_DIRECTOR.FILM_ID LEFT JOIN MPA ON MPA.ID=FILM.MPA_ID " +
                        "ORDER BY FILM.RATE DESC";
                List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm);
                return returnFilmWithCorrectGenre(films);
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        } else if (by.equals("title")) {
            try {
                log.debug("Возвращаем поиск по названию с сортировкой по лайкам");
                String sqlQuery = "SELECT FILM.*, MPA.* FROM FILM LEFT JOIN MPA ON FILM.MPA_ID = MPA.ID " +
                        "WHERE UPPER(FILM.NAME) LIKE UPPER('%" + query + "%') ORDER BY FILM.RATE DESC";
                List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm);
                return returnFilmWithCorrectGenre(films);
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        } else if (by.equals("director,title") || by.equals("title,director")) {
            try {
                log.debug("Возвращаем поиск по режиссеру и по названию с сортировкой по лайкам");
                String sqlQuery = "SELECT * from  FILM " +
                        "LEFT JOIN MPA ON FILM.MPA_ID = MPA.ID " +
                        "LEFT JOIN FILMS_OF_DIRECTOR ON FILM.ID = FILMS_OF_DIRECTOR.FILM_ID " +
                        "LEFT JOIN DIRECTORS ON FILMS_OF_DIRECTOR.DIRECTORS_ID = DIRECTORS.ID " +
                        "where UPPER(FILM.NAME) LIKE UPPER('%" + query + "%') OR " +
                        "UPPER(DIRECTORS.NAME) LIKE UPPER('%" + query + "%') ORDER BY FILM.ID DESC";
                List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm);
                return returnFilmWithCorrectGenre(films);
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
        return null;
    }

    // Метод, для возврата списка фильмов с полем genre = null, там где оно пустое
    public List<Film> returnFilmWithCorrectGenre (List<Film> films) {
        List<Film> returnFilms = new ArrayList<>();
        for (Film film: films) {
            if (film.getGenres().isEmpty()) {
                film.setGenres(null);
            }
            returnFilms.add(film);
        }
        return returnFilms;
    }
    private Film makeFilm(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Собираем объект в методе makeFilm");
        Film film = new Film(
                resultSet.getLong("FILM.ID"),
                resultSet.getString("FILM.NAME"),
                resultSet.getString("FILM.DESCRIPTION"),
                resultSet.getLong("FILM.DURATION"),
                resultSet.getDate("FILM.RELEASE_DATE").toLocalDate(),
                new MPA(resultSet.getInt("MPA.ID"), resultSet.getString("MPA.NAME")),
                Set.of(new Genre(0, "EMPTY")),
                resultSet.getLong("RATE"),
                Set.of(new FilmDirector(0L, "EMPTY"))
        );
        Set<Genre> genres = genreDbStorage.getGenresByFilmId(film.getId());
        if (!genres.isEmpty()) {
            Set<Genre> sortedGenres = new TreeSet<>(Comparator.comparing(Genre::getId));
            sortedGenres.addAll(genres);
            genres = sortedGenres;
        }
        film.setGenres(genres);

        Set<FilmDirector> directors = filmDirectorsDBStorage.getDirectorsByFilmId(film.getId());
        if (!directors.isEmpty()) {
            Set<FilmDirector> sortedDirectors = new TreeSet<>(Comparator.comparing(FilmDirector::getId));
            sortedDirectors.addAll(directors);
            directors = sortedDirectors;
        }
        film.setDirectors(directors);
        return film;
    }

/*    private Film makeFilmForSearch(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Собираем объект в методе makeFilm");
        Film film = new Film(
                resultSet.getLong("ID"),
                resultSet.getString("NAME"),
                resultSet.getString("DESCRIPTION"),
                resultSet.getLong("DURATION"),
                resultSet.getDate("RELEASE_DATE").toLocalDate(),
                new MPA(resultSet.getInt("MPA.ID"), resultSet.getString("MPA.NAME")),
                Set.of(new Genre(0, "EMPTY")),
                resultSet.getLong("RATE"),
                Set.of(new FilmDirector(0L, "EMPTY"))
        );

        Set<Genre> genres = genreDbStorage.getGenresByFilmId(film.getId());
        if (!genres.isEmpty()) {
            Set<Genre> sortedGenres = new TreeSet<>(Comparator.comparing(Genre::getId));
            sortedGenres.addAll(genres);
            genres = sortedGenres;
        } else {
            genres = null;
        }
        film.setGenres(genres);

        Set<FilmDirector> directors = filmDirectorsDBStorage.getDirectorsByFilmId(film.getId());
        if (!directors.isEmpty()) {
            Set<FilmDirector> sortedDirectors = new TreeSet<>(Comparator.comparing(FilmDirector::getId));
            sortedDirectors.addAll(directors);
            directors = sortedDirectors;
        }
        film.setDirectors(directors);
        return film;
    }*/

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) { // получить Общие фильмы
        String sqlQuery = "SELECT film_id FROM like_status WHERE user_id = ? INTERSECT " +
                "SELECT film_id FROM like_status WHERE user_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, userId, friendId);
        List<Film> commonFilms = new ArrayList<>();
        while (rowSet.next()) {
            commonFilms.add(getFilmById(rowSet.getInt("film_id")));
        }
        return commonFilms;
    }


    public void deleteAll() {
        String sqlQuery = "DELETE FROM film";
        jdbcTemplate.update(sqlQuery);
    }
}
