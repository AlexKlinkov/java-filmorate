package ru.yandex.practicum.filmorate.validators;

import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

@Service
public class DataValidator implements ConstraintValidator<AfterOrEqualData, LocalDate> {
    private LocalDate data;

    public void initialize(AfterOrEqualData annotation) {
        data = LocalDate.parse(annotation.value());
    }

    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        boolean valid = true;
        if (value != null) {
            if (value.isBefore(data)) {
                valid = false;
            }
        }
        return valid;
    }
}
