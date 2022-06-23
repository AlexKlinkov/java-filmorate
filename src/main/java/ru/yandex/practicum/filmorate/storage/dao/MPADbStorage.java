package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class MPADbStorage {
    private final JdbcTemplate jdbcTemplate;

    public MPADbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MPA> getMPAs() {
        try {
            log.debug("Возвращаем список с рейтингами");
            return jdbcTemplate.query("select * from MPA", this::makeMPA);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть список со всеми рейтингами возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    public MPA getGenreById(int id) {
        if (id < 0) {
            log.debug("При попытке вернуть возрастной рейтинг возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from GENRE where ID = ?", id);
        if (!filmRows.first()) {
            log.debug("При получения рейтинга возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                return getMPAs().get(0);
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть возрастной рейтинг возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
    }

    public MPA makeMPA(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Собираем объект в методе makeMPA");
        return new MPA(
                resultSet.getInt("ID"),
                resultSet.getString("NAME")
        );
    }
}
