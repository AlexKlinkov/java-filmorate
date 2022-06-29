package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.sql.Timestamp;
import java.time.Instant;

@Data
public class Event {
    private long eventId; // Ключ таблицы, генирируется таблицей самостоятельно
    private long userId; // ID пользователя, которому принадлежит событие
    private long timestamp; // Дата и время события
    private String eventType; // Тип события, одно из значениий LIKE, REVIEW или FRIEND
    private String operation; // Вид события, одно из значениий REMOVE, ADD, UPDATE
    private long entityId; // Идентификатор сущности, с которой произошло событие

    public Event(long eventId, long userId, long timestamp, String eventType, String operation, long entityId) {
        this.eventId = eventId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
    }
}
