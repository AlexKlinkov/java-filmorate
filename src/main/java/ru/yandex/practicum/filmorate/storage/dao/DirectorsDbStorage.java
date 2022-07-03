package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FilmDirector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Slf4j
public class DirectorsDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public DirectorsDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public FilmDirector create(FilmDirector filmDirector) throws RuntimeException {
        if (filmDirector == null) {
            log.debug("При попытке создать нового режиссера произошла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        }
        log.debug("При создании режиссера, что данного режиссера еще нет в БД");
        SqlRowSet alreadyExist = jdbcTemplate.queryForRowSet("select * from DIRECTORS where NAME = ? " +
                        "AND ID = ?",
                filmDirector.getName(), filmDirector.getId());
        if (alreadyExist.first()) {
            log.debug("Если режиссер уже есть в БД, то не создаем его, а возвращаем из БД, " +
                    "обеспечивая уникальность данных");
            alreadyExist.beforeFirst();
            while (alreadyExist.next()) {
                FilmDirector returnDirector = getDirectorById(alreadyExist.getLong("ID"));
                return returnDirector;
            }
        }
        try {
            log.debug("Возвращаем и добавляем режиссера в БД");
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("DIRECTORS").usingGeneratedKeyColumns("ID");
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("NAME", filmDirector.getName());
            Number num = jdbcInsert.executeAndReturnKey(parameters);
            filmDirector.setId(num.longValue());
            return filmDirector;
        } catch (RuntimeException e) {
            log.debug("При попытке создать нового директора произошла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    public FilmDirector getDirectorById(long id) {
            if (id < 0) {
                log.debug("При попытке вернуть режиссера возникла ошибка с ID");
                throw new NotFoundException("Искомый объект не найден");
            }
            SqlRowSet directorRows = jdbcTemplate.queryForRowSet("select * from DIRECTORS where ID = ?", id);
            if (!directorRows.first()) {
                log.debug("При получении директора возникла ошибка с NULL");
                throw new NotFoundException("Искомый объект не найден");
            } else {
                try {
                    FilmDirector filmDirector = null;
                    directorRows.beforeFirst();
                    if (directorRows.next()) {
                        filmDirector = new FilmDirector(directorRows.getLong("ID"),
                                directorRows.getString("NAME"));
                    }
                    return filmDirector;
                } catch (RuntimeException e) {
                    log.debug("При попытке вернуть режиссера возникла внутренняя ошибка сервера");
                    throw new RuntimeException("Внутреняя ошибка сервера");
                }
            }
        }

    public FilmDirector update(FilmDirector filmDirector) throws RuntimeException {
        if (filmDirector == null) {
            log.debug("При обновлении режиссера передали значение Null");
            throw new ValidationException("Ошибка валидации");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from DIRECTORS where ID = ?",
                filmDirector.getId());
        if (!sqlRowSet.first()) {
            log.debug("При обновлении режиссера объект с ID - " + filmDirector.getId() + " не был найден");
            throw new NotFoundException("Искомый объект не найден");
        } else {
            try {
                String sqlQuery = "UPDATE DIRECTORS SET NAME = ? where  ID = ?";
                jdbcTemplate.update(sqlQuery,
                        filmDirector.getName(), filmDirector.getId());
                return filmDirector;
            } catch (RuntimeException e) {
                log.debug("При обновлении режиссера возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }


    public void delete(Long filmDirectorId) throws RuntimeException {
        if (filmDirectorId.equals(null)) {
            log.debug("При удаления режиссера возникла ошибка с NULL");
            throw new NotFoundException("Искомый объект не найден");
        }
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from DIRECTORS where ID = ?", filmDirectorId);
        if (!sqlRowSet.first()) {
            log.debug("При удалении режиссера возникла ошибка с ID");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                jdbcTemplate.update("delete from DIRECTORS where ID = ?", filmDirectorId);
                log.debug("Удалили режиссера");
            } catch (RuntimeException e) {
                log.debug("При удалении режиссера возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    public void validateDirector(Long id) {
        int exist = jdbcTemplate.queryForObject("select COUNT(*) from DIRECTORS where ID = ?", Integer.class, id);
        if (exist==0) {
            throw new NotFoundException("Искомый объект не найден");
        }
    }
    public List<FilmDirector> getDirectors() throws RuntimeException {
        try {
            log.debug("Возвращаем список со всеми режиссерами");
            String sqlQuery = "SELECT * from DIRECTORS;";
            return jdbcTemplate.query(sqlQuery, this::makeDirector);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми фильмами возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    private FilmDirector makeDirector(ResultSet resultSet, int i) throws SQLException {
        log.debug("Собираем объект в методе makeDirector");
        FilmDirector filmDirector = new FilmDirector(
                resultSet.getLong("DIRECTORS.ID"),
                resultSet.getString("DIRECTORS.NAME")
        );
        return filmDirector;
    }

}

