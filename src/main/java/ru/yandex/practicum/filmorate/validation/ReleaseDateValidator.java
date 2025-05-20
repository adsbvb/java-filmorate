package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidator;

import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<ReleaseDateConstraint, LocalDate> {
    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        final LocalDate CINEMATOGRAPHY_BIRTHDAY = LocalDate.of(1895, 12, 28);
        if (localDate != null) {
            return localDate.isAfter(CINEMATOGRAPHY_BIRTHDAY);
        }
        return true;
    }
}
