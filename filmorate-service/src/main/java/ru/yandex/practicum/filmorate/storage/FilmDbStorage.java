package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.helpers.connector.ConnectToDB;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDirector;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@RequiredArgsConstructor
@Repository
public class FilmDbStorage {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private final ConnectToDB connectToDB;
    @Autowired
    private final FilmsGenresDbStorage filmsGenresDbStorage;
    @Autowired
    private final MPADbStorage mpaDbStorage;
    @Autowired
    private final GenreDbStorage genreDbStorage;
    @Autowired
    private final LikeStatusDbStorage likeStatusDbStorage;
    @Autowired
    private final DirectorsDbStorage directorsDbStorage;
    @Autowired
    private final FilmDirectorsDBStorage filmDirectorsDBStorage;

    public Film create(Film film) throws RuntimeException {
        if (film == null) {
            log.debug("При попытке создать новый фильм произошла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        }
        log.debug("При создании фильма проверяем, что данного фильма еще нет в БД");
        SqlRowSet alreadyExist = jdbcTemplate.queryForRowSet("select * from film where name = ? " +
                        "and description = ? and release_date = ?",
                film.getName(), film.getDescription(), film.getReleaseDate());
        if (alreadyExist.first()) {
            log.debug("Если фильм уже есть в БД, то не создаем его, а возвращаем из БД, " +
                    "обеспечивая уникальность данных");
            alreadyExist.beforeFirst();
            while (alreadyExist.next()) {
                Film returnFilm = getFilmById(alreadyExist.getLong("id"));
                if (returnFilm.getGenres() == null || returnFilm.getGenres().isEmpty()) {
                    returnFilm.setGenres(null);
                }
                return returnFilm;
            }
        }
        try {
            log.debug("Возвращаем и добавляем фильм в БД");
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("film").usingGeneratedKeyColumns("id");
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("name", film.getName())
                    .addValue("description", film.getDescription())
                    .addValue("duration", film.getDuration())
                    .addValue("release_date", Date.valueOf(film.getReleaseDate()))
                    .addValue("rate", film.getRate())
                    .addValue("mpa_id", film.getMpa().getId());
            Number num = jdbcInsert.executeAndReturnKey(parameters);
            film.setId(num.intValue());
            log.debug("При создании фильма запоняем возрастной рейтинга по ID");
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

    public Film update(Film film) throws RuntimeException, SQLException {
        if (film == null) {
            log.debug("При обновлении фильма передали значение Null");
            throw new ValidationException("Ошибка валидации");
        }
        Film filmFromBD = getFilmById(film.getId());
        if (filmFromBD == null) {
            log.debug("При обновлении фильма объект с ID - " + film.getId() + " не был найден");
            throw new NotFoundException("Искомый объект не найден");
        } else {
            try {
                String sqlQuery = "update film set " +
                        "name = '" + film.getName() + "', description = '" + film.getDescription() + "', duration = '" +
                        film.getDuration() + "', release_date = '" + film.getReleaseDate() +
                        "', mpa_id = '" + film.getMpa().getId() + "' where id = " + film.getId();
                connectToDB.getStatement().executeUpdate(sqlQuery);
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

    public void delete(Film film) throws RuntimeException {
        if (film == null) {
            log.debug("При удаления фильма возникла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from film where id = ?", film.getId());
        if (!sqlRowSet.first()) {
            log.debug("При удалении фильма возникла ошибка с ID");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Удалили фильм");
                filmsGenresDbStorage.deleteFilmAndGenreByFilmId(film.getId());
                likeStatusDbStorage.deleteLikeByFilmId(film.getId());
                jdbcTemplate.update("delete from film where id = ?", film.getId());
            } catch (RuntimeException e) {
                log.debug("При удалении фильма возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    public void deleteById(long id) {
        if (id < 0) {
            log.debug("При попытке удалить фильм возникла ошибка с ID: {}", id);
            throw new NotFoundException("Искомый объект не может быть найден");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from film where id = ?", id);
        if (!sqlRowSet.first()) {
            log.debug("При удалении фильма возникла ошибка с ID: {}", id);
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Удалили фильм");
                filmsGenresDbStorage.deleteFilmAndGenreByFilmId(id);
                likeStatusDbStorage.deleteLikeByFilmId(id);
                jdbcTemplate.update("delete from film where id = ?", id);
            } catch (RuntimeException e) {
                log.debug("При удалении фильма возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    public List<Film> getFilms() throws RuntimeException {
        try {
            log.debug("Возвращаем список со всеми фильмами");
            String sqlQuery = "select * from  film " +
                    "left join mpa on film.mpa_id = mpa.id";
            return jdbcTemplate.query(sqlQuery, this::makeFilm);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    public Film getFilmById(long id) throws RuntimeException {
        if (id < 0) {
            log.debug("При попытке вернуть фильм возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from film where id = ?", id);
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

    public List<Film> getFilmsByDirectorSortedByYear(Long directorId) {
        directorsDbStorage.validateDirector(directorId);
        try {
            log.debug("Возвращаем отсортированный список по году одного режиссера");
            String sqlQuery = String.format("select film.*, mpa.* from (select film_id from films_of_director where " +
                    "directors_id= %s) A left join film on film.id=A.film_id left join mpa on film.mpa_id = mpa.id" +
                    "                    order by film.release_date asc", directorId);
            List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm);
            return returnFilmWithCorrectGenre(films);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    public List<Film> FilmsOfOneDirector(Long directorId) {
        directorsDbStorage.validateDirector(directorId);
        try {
            log.debug("Возвращаем поиск по ID режиссера");
            String sqlQuery = "select film.*, mpa.* from (select film_id from films_of_director where directors_id=" +
                    directorId + ") A left join film on film.id=A.film_id left join mpa on film.mpa_id = mpa.id";
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
                String sqlQuery = "select film.*, film.* from (select * from directors where upper(name) like upper('%"
                        + query + "%')) D left join films_of_director on D.id=films_of_director.directors_id " +
                        "left join film on film.id=films_of_director.film_id left join mpa on mpa.id=film.mpa_id " +
                        "order by film.rate desc";
                List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm);
                return returnFilmWithCorrectGenre(films);
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        } else if (by.equals("title")) {
            try {
                log.debug("Возвращаем поиск по названию с сортировкой по лайкам");
                String sqlQuery = "select film.*, mpa.* from film left join mpa on film.mpa_id = mpa.id " +
                        "where upper(film.name) like upper('%" + query + "%') order by film.rate desc";
                List<Film> films = jdbcTemplate.query(sqlQuery, this::makeFilm);
                return returnFilmWithCorrectGenre(films);
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        } else if (by.equals("director,title") || by.equals("title,director")) {
            try {
                log.debug("Возвращаем поиск по режиссеру и по названию с сортировкой по лайкам");
                String sqlQuery = "select * from film " +
                        "left join mpa on film.mpa_id = mpa.id " +
                        "left join films_of_director on film.id = films_of_director.film_id " +
                        "left join directors on films_of_director.directors_id = directors.id " +
                        "where upper(film.name) like upper('%" + query + "%') or " +
                        "upper(directors.name) like upper('%" + query + "%') order by film.id desc";
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
    public List<Film> returnFilmWithCorrectGenre(List<Film> films) {
        List<Film> returnFilms = new ArrayList<>();
        for (Film film : films) {
            if (film.getGenres().isEmpty()) {
                film.setGenres(null);
            }
            returnFilms.add(film);
        }
        return returnFilms;
    }

    private Film makeFilm(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Собираем объект в методе makeFilm");
        MPA mpa = mpaDbStorage.getMPAById(resultSet.getLong("mpa_id"));
        Film film = new Film(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getLong("duration"),
                resultSet.getDate("release_date").toLocalDate(),
                mpa,
                Set.of(new Genre(0, "EMPTY")),
                resultSet.getLong("rate"),
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

    public Collection<Film> getCommonFilms(Long userId, Long friendId) { // получить Общие фильмы
        String sqlQuery = "select film_id from like_status where user_id = ? intersect " +
                "select film_id from like_status where user_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, userId, friendId);
        List<Film> commonFilms = new ArrayList<>();
        while (rowSet.next()) {
            commonFilms.add(getFilmById(rowSet.getInt("film_id")));
        }
        return commonFilms;
    }
}
