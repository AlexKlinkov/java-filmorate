package ru.yandex.practicum.filmorate.exception;

public class ValidationExceptionFilmorate extends RuntimeException{
    public ValidationExceptionFilmorate(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
