package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventOfUserDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public EventOfUserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Метод по заполнению таблицы БД "EVENT_OF_USER"
    public void addNumberOfEventToUser(long eventId, long userId) {
        log.debug("Заполняем таблицу EVENT_OF_USER в методе addNumberOfEventToUser");
        jdbcTemplate.update("MERGE INTO EVENT_OF_USER (EVENTID, USERID)  VALUES (?, ?)", eventId, userId);
    }
}
