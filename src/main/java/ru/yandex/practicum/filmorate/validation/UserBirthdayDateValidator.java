package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class UserBirthdayDateValidator implements ConstraintValidator<UserBirthdayDateConstraint, LocalDate> {
    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        if (localDate != null) {
            return localDate.isBefore(LocalDate.now());
        }
        return true;
    }
}
