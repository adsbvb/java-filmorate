package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UserBirthdayDateValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UserBirthdayDateConstraint {
    String message() default "Некорректная дата рождения";

    Class<?> [] groups() default {};

    Class<? extends Payload>[] payload() default {};
}