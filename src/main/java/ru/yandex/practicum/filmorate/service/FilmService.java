package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@RequiredArgsConstructor
@Service
public class FilmService {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private final FilmDbStorage filmStorage;
    @Autowired
    private final UserDbStorage userStorage;
    @Autowired
    private final LikeStatusDbStorage likeStatusDbStorage;
    @Autowired
    private final MPADbStorage mpaDbStorage;
    @Autowired
    private final GenreDbStorage genreDbStorage;
    @Autowired
    private final EventDbStorage eventDbStorage;
    @Autowired
    private final FilmDirectorsDBStorage filmDirectorsDBStorage;

    // Метод по добавлению лайка
    public void addLike(Long filmId, Long userId) throws SQLException {
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
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Метод по удалению лайка
    public void deleteLike(Long filmId, Long userId) throws SQLException {
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
                    eventDbStorage.addEvent(userId, filmId, "LIKE", "REMOVE");
                }
            } catch (RuntimeException e) {
                log.debug("При удалении лайка к фильму возникла внутренняя ошибка серввера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Метод отражает 10 самых популярных фильмов на основе количества лайков у каждого или заданое число фильмов
    public List<Film> displayTenTheMostPopularFilmsIsParamIsNotDefined(long count) {
        try {
            long amount = 10; // Значение по умолчанию
            if (count > 0) {
                amount = count;
            }
            log.debug("Возвращаем список с самыми популярными фильмами");
            List<Film> films = filmStorage.getFilms();
            List<Film> returnFilms = new ArrayList<>();
            for (Film film : films) {
                film.setRate(likeStatusDbStorage.getAmountOfLikesOfFilmByFilmId(film.getId()));
                returnFilms.add(film);
            }
            return returnFilms.stream()
                    .sorted((o1, o2) -> (int) (o2.getRate() - o1.getRate()))
                    .limit(amount)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Внутренняя ошибка сервера");
        }
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) throws SQLException {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Не найден пользователь с идентификатором: " + user.getId());
        }
        User friend = userStorage.getUserById(friendId);
        if (friend == null) {
            throw new NotFoundException("Не найден пользователь с идентификатором: " + friend.getId());
        }
        return new ArrayList<>(filmStorage.getCommonFilms(userId, friendId));

    }

    public List<Film> mostPopularsByGenreYear(long count, Integer generId, Integer year) {
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
                if (f.getReleaseDate().getYear() == year && f.getGenres().contains(
                        genreDbStorage.getOneById(generId))) {
                    listToReturn.add(f);
                }
            }
        }
        if (count == 0) {
            return listToReturn.stream().limit(1).collect(Collectors.toList());
        }
        return listToReturn;
    }

    // метод возвращает список рекомендуемых фильмов для пользователя,
    // основан на поиске пользователя с аналогичными лайками фильмов
    public List<Film> getRecommendations(Long userId) {
        Map<Long, Set<Long>> likes = new HashMap<>();
        List<Film> recommendedFilms = new ArrayList<>();

        String sql = "SELECT film_id, user_id FROM like_status";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);

        while (rs.next()) {
            Long userIdAsKey = rs.getLong("user_id");
            if (likes.get(userIdAsKey) == null) {
                likes.put(userIdAsKey, new HashSet<>());
            }
            likes.get(userIdAsKey).add(rs.getLong("film_id"));
        }

        if (!likes.isEmpty()) {

            Integer intersectionSize;
            Integer maxSize = 0;
            Long matchingUserId = null;

            for (Long id : likes.keySet()) {
                Set<Long> likesUser = new HashSet<>(likes.get(userId));
                if (id == userId) {
                    intersectionSize = 0;
                } else {
                    likesUser.retainAll(likes.get(id));
                    intersectionSize = likesUser.size();
                }
                if (intersectionSize >= maxSize) {
                    maxSize = intersectionSize;
                    matchingUserId = id;
                }
            }
            List<Long> diff = new ArrayList<>(likes.get(matchingUserId));
            diff.removeAll(likes.get(userId));
            log.debug("Наиболее похожие предпочтения у пользователя с ID={}", matchingUserId);
            for (Long filmId : diff) {
                recommendedFilms.add(filmStorage.getFilmById(filmId));
            }
        }
        return recommendedFilms;
    }
}
