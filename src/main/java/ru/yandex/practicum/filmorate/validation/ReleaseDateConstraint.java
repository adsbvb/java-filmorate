package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Payload;
import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ReleaseDateValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ReleaseDateConstraint {
    String message() default "Некорректная дата релиза";

    Class<?> [] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
