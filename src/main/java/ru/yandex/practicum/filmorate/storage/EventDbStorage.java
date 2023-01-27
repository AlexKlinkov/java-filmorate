package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.helpers.connector.ConnectToDB;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@RequiredArgsConstructor
@Repository
public class EventDbStorage {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private final ConnectToDB connectToDB;
    @Autowired
    private final EventOfUserDbStorage eventOfUserDbStorage;


    // Метод добавляет событие в таблицы БД
    public Event addEvent(long userId, long entityId, String eventType, String operation) {
        log.debug("Добавляем событие в таблицу");
        try {
            String query = "insert into event (timeStamp, eventType, operation, entityId) values " +
                    "('" + Timestamp.from(Instant.now()) + "', '" +
                    eventType + "', '" + operation + "', '" + entityId + "')";
            long eventId = connectToDB.getStatement().executeUpdate(query);
            eventOfUserDbStorage.addNumberOfEventToUser(eventId, userId);
            return getOneEventById(eventId);
        } catch (RuntimeException e) {
            log.debug("При попытке записать событие в БД произошла внутреняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Метод по возвращению ленты событий пользователя (самые новые, наверху)
    public List<Event> getRibbonOfEventsOfUserByUserId(long userId) {
        try {
            log.debug("Возвращаем ленту событий пользователя");
            String query = "select event.eventId," +
                    "event_of_user.userId," +
                    "event.timeStamp," +
                    "event.eventType," +
                    "event.operation," +
                    "event.entityId " +
                    "from event " +
                    "left join event_of_user ON event.eventId = event_of_user.eventId " +
                    "where event_of_user.userId = " + userId + " " +
                    "group by event.timeStamp " +
                    "order by event.timeStamp";
            return jdbcTemplate.query(query, this::makeEvent);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть ленту событий пользователя возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    // Метод возвращает одно событие пользователя по ID события
    public Event getOneEventById(long eventId) throws SQLException {
        if (eventId < 0) {
            log.debug("При попытке вернуть событие возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        ResultSet eventRows = connectToDB.getStatement().executeQuery("select * from event where eventId = "
                + eventId);
        if (!eventRows.next()) {
            log.debug("При получении события возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Получаем ID пользователя, в методе по возвращению одного " +
                        "события по ID события 'getOneEventById' ");
                ResultSet sqlRowSet = connectToDB.getStatement().executeQuery("select * from event_of_user" +
                        " where eventId = " + eventId);
                long userId;
                if (sqlRowSet.getRow() == 0) {
                    log.debug("При попытке вернуть событие возникла ошибка с ID пользователя");
                    return null;
                } else {
                    userId = sqlRowSet.getLong(1);
                }
                List<Event> events = getRibbonOfEventsOfUserByUserId(userId);
                for (Event event : events) {
                    if (event.getEventId() == eventId) {
                        return getRibbonOfEventsOfUserByUserId(userId).get(
                                new ArrayList<>(events).indexOf(event));
                    }
                }
            } catch (RuntimeException e) {
                log.debug("При попытке вернуть событие пользователя возникла внутренняя ошибка сервера");
                throw new RuntimeException("Внутреняя ошибка сервера");
            }
        }
        return null;
    }

    // Метод по созданию объекта Event из значений, хранящихся в БД
    public Event makeEvent(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Создаем событие в методе makeEvent");
        Event event = new Event(
                resultSet.getLong("eventId"),
                0,
                resultSet.getTimestamp("timeStamp").toInstant().toEpochMilli(),
                resultSet.getString("eventType"),
                resultSet.getString("operation"),
                resultSet.getLong("entityId")
        );
        log.debug("Добавляем ID пользователя в объект событие в методе makeEvent");
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select userId from event_of_user where eventId = ?",
                resultSet.getLong("eventId"));
        if (sqlRowSet.next()) {
            log.debug("В методе makeEvent устанавливаем ID пользователя для объекта");
            event.setUserId(sqlRowSet.getLong("userId"));
        }
        return event;
    }
}
