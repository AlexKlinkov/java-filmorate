package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class EventController {

    @Autowired
    private final EventService eventService;

    // Метод возвращает ленту событий пользователя
    @GetMapping("/{id}/feed")
    public List<Event> getListWithEventsOfUser(@PathVariable long id) throws RuntimeException {
        return eventService.getRibbonOfEventsOfUser(id);
    }
}
