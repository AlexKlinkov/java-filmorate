package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.dao.EventDbStorage;

import java.util.List;

@Service
public class EventService {

    private final EventDbStorage eventDbStorage;

    @Autowired
    public EventService(EventDbStorage eventDbStorage) {
        this.eventDbStorage = eventDbStorage;
    }

    // Метод по возвращению леты событий пользователя по userID
    public List<Event> getRibbonOfEventsOfUser (long userid) {
        return eventDbStorage.getRibbonOfEventsOfUserByUserId(userid);
    }
}
