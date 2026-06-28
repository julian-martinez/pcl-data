package me.julianmartinez.pcliga.web.scraper.application.shared.model.exception;

import lombok.Getter;
import me.julianmartinez.pcliga.web.scraper.infrastructure.exception.ErrorCode;

import java.io.Serial;

@Getter
public abstract class AppException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3109729620782927689L;

    private final ErrorCode errorCode;

    protected AppException(final ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
