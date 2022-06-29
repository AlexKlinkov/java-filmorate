package ru.yandex.practicum.filmorate.exception;

public class NotFoundExceptionFilmorate extends RuntimeException {
    public NotFoundExceptionFilmorate(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
