package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.ErrorResponse;

@RestControllerAdvice
public class ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleNotFound(final NotFoundException e) {
        logger.error("NotFoundException: " + e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidation(final ValidationException e) {
        logger.error("ValidationException: " + e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidation(final MethodArgumentNotValidException e) {
        logger.error("MethodArgumentNotValidException: " + e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidation(final ConstraintViolationException e) {
        logger.error("ConstraintViolationException: " + e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleThrowable(final Throwable e) {
        logger.error("error: " + e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
