package me.julianmartinez.pcliga.web.scraper.infrastructure.controller;

import me.julianmartinez.pcliga.web.scraper.application.shared.model.exception.AppException;
import me.julianmartinez.pcliga.web.scraper.infrastructure.exception.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiError> handleAppException(final AppException exception) {

        final HttpStatus status = exception.getErrorCode().getHttpStatus();
        final ApiError error = ApiError.create(
            exception.getErrorCode().getCode(),
            exception.getMessage(),
            status.value(),
            LocalDateTime.now().toString()
        );

        return ResponseEntity.status(status).body(error);
    }
}
