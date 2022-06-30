package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundExceptionFilmorate;
import ru.yandex.practicum.filmorate.exception.ValidationExceptionFilmorate;

@RestControllerAdvice
public class ErrorHandler {

    // 400 — если ошибка валидации: ValidationException
    @ExceptionHandler(ValidationExceptionFilmorate.class)
    public ResponseEntity<?> handleNotCorrectValidate(ValidationExceptionFilmorate exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 404 — для всех ситуаций, если искомый объект не найден
    @ExceptionHandler(NotFoundExceptionFilmorate.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundExceptionFilmorate exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 500 — если возникло исключение
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleServerError(RuntimeException exception) {
        return new ResponseEntity("Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
