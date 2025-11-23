package com.exammate.exammate_backend.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.exammate.exammate_backend.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleApiException(ApiException ex) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException ex) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .build();
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationErrorsHandler(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors();
        // Build messages of the form: "field defaultMessage" e.g. "questionLimit must be greater than or equal to 5"
        String errors = fieldErrors
                .stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        String keys = fieldErrors
                .stream()
                .map(FieldError::getField)
                .distinct()
                .collect(Collectors.joining(","));
        logger.warn("Validation failed: {} (fields={})", errors, keys);
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error(errors.isEmpty() ? "Validation failed" : errors)
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    // New handler for type mismatches (e.g. invalid UUID in path variable)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        Object valueObj = ex.getValue();
        String value = valueObj == null ? "null" : String.valueOf(valueObj);
        Class<?> required = ex.getRequiredType();

        String msg;
        if (java.util.UUID.class.equals(required)) {
            msg = String.format("Invalid %s: '%s' is not a valid UUID", name, value);
        } else {
            msg = String.format("Invalid value for parameter '%s': '%s'", name, value);
        }
        logger.warn("Type mismatch for parameter {}: requiredType={}, value={}", name, required, value);
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error(msg)
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.error("Database constraint violation", ex);
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error("Invalid request data.")
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    @ExceptionHandler({ConstraintViolationException.class, PersistenceException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlePersistenceExceptions(Exception ex) {
        logger.error("Persistence error", ex);
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error("Invalid request data.")
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOtherExceptions(Exception ex) {
        logger.error("Internal Server Error", ex);
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .error("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
    }

}
