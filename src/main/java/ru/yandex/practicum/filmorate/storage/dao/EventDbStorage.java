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
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class EventDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final EventOfUserDbStorage eventOfUserDbStorage;

    public EventDbStorage(JdbcTemplate jdbcTemplate, EventOfUserDbStorage eventOfUserDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventOfUserDbStorage = eventOfUserDbStorage;
    }

    // Метод добавляет событие в таблицы БД
    public Event addEvent (long userId, long entityId, String eventType, String operation) {
        try {
            log.debug("Добавляем событие в БД");
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("EVENT").usingGeneratedKeyColumns("eventId");
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("timeStamp", Timestamp.from(Instant.now()))
                    .addValue("eventType", eventType)
                    .addValue("operation", operation)
                    .addValue("entityId", entityId);
            Number num = jdbcInsert.executeAndReturnKey(parameters);
            eventOfUserDbStorage.addNumberOfEventToUser(num.longValue(), userId);
            Event event = getOneEventById(num.longValue());
            return event;
        } catch (RuntimeException e) {
            log.debug("При попытке записать событие в БД произошла внутреняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    // Метод по возвращению ленты событий пользователя (самые новые, наверху)
    public List<Event> getRibbonOfEventsOfUserByUserId(long userId) {
        try {

            log.debug("Возвращаем ленту событий пользователя");
            return jdbcTemplate.query("select EVENT.EVENTID," +
                    "EVENT_OF_USER.USERID," +
                    "EVENT.TIMESTAMP," +
                    "EVENT.EVENTTYPE," +
                    "EVENT.OPERATION," +
                    "EVENT.ENTITYID, " +
                    "from EVENT " +
                    "left join EVENT_OF_USER ON EVENT.EVENTID = EVENT_OF_USER.EVENTID " +
                    "where EVENT_OF_USER.USERID = " + userId + " " +
                    "GROUP BY EVENT.TIMESTAMP " +
                    "ORDER BY EVENT.TIMESTAMP", this::makeEvent);
        } catch (RuntimeException e) {
            log.debug("При попытке вернуть ленту событий пользователя возникла внутренняя ошибка сервера");
            throw new RuntimeException("Внутреняя ошибка сервера");
        }
    }

    // Метод возвращает одно событие пользователя по ID события
    public Event getOneEventById (long eventId) {
        if (eventId < 0) {
            log.debug("При попытке вернуть событие возникла ошибка с ID");
            throw new NotFoundException("Искомый объект не найден");
        }
        SqlRowSet eventRows = jdbcTemplate.queryForRowSet("select * from EVENT where EVENTID = ?", eventId);
        if (!eventRows.first()) {
            log.debug("При получении события возникла ошибка с NULL");
            throw new ValidationException("Ошибка валидации");
        } else {
            try {
                log.debug("Получаем ID пользователя, в методе по возвращению одного события по ID события 'getOneEventById' ");
                SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("select * from EVENT_OF_USER where EVENTID = ?", eventId);
                long userId = -1;
                if (sqlRowSet.next()) {
                    userId = sqlRowSet.getLong("userId");
                }
                List<Event> events = getRibbonOfEventsOfUserByUserId(userId);
                for (Event event : events) {
                    if (event.getEventId() == eventId) {
                        Event returnEvent = getRibbonOfEventsOfUserByUserId(userId).get(new ArrayList<>(events).indexOf(event));
                        return returnEvent;
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
                resultSet.getTimestamp("timestamp").toInstant().toEpochMilli(),
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
