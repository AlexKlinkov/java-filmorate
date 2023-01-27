package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.helpers.connector.ConnectToDB;

import java.sql.*;

@Slf4j
@Data
@RequiredArgsConstructor
@Repository
public class EventOfUserDbStorage {

    @Autowired
    private final ConnectToDB connectToDB;

    // Метод по заполнению таблицы БД "EVENT_OF_USER"
    public void addNumberOfEventToUser(long eventId, long userId) throws SQLException {
        log.debug("Заполняем таблицу EVENT_OF_USER в методе addNumberOfEventToUser, " +
                "Проеряем, что такой записи  таблице event_of_user еще нет");
        ResultSet first = connectToDB.getStatement().executeQuery(
                "select count(*) from event_of_user where eventId = " + eventId + " and userId = " + userId
        );
        ResultSet second = connectToDB.getStatement().executeQuery(
                "select count(*) from event_of_user where eventId = " + userId + " and userId = " + eventId
        );
        if (first.getRow() == 0) {
            log.debug("Данная запись уже есть в таблице event_of_user");
            return;
        } else if (second.getRow() == 0) {
            log.debug("Данная запись уже есть в таблице event_of_user");
            return;
        }
        log.debug("Проверяем, есть ли событие с таким ID в таблице event в методе addNumberOfEventToUser," +
                "Иначе будет нарушена целостьность БД");
        connectToDB.getStatement().executeUpdate(
                "insert into event_of_user (eventId, userId) values ('" + eventId + "', '" + userId + "')"
        );
    }

    // Метод по удалению записи по ID пользователя
    public void deleteRecordFromTableEventOfUserByUserId(long userid) throws SQLException {
        log.debug("Удаляем запись из таблицы Event_of_user");
        connectToDB.getStatement().executeUpdate("delete from event_of_user where userId = " + userid);
    }
}
