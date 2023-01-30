package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventDbStorage;

import java.util.List;

@Data
@RequiredArgsConstructor
@Service
public class EventService {

    @Autowired
    private final EventDbStorage eventDbStorage;

    // Метод по возвращению леты событий пользователя по userID
    public List<Event> getRibbonOfEventsOfUser(long userid) {
        return eventDbStorage.getRibbonOfEventsOfUserByUserId(userid);
    }
}
