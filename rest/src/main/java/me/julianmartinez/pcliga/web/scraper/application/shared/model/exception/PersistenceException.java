package me.julianmartinez.pcliga.web.scraper.application.shared.model.exception;

import me.julianmartinez.pcliga.web.scraper.infrastructure.exception.ErrorCode;

import java.io.Serial;

public class PersistenceException extends AppException {

    @Serial
    private static final long serialVersionUID = 2254080077609174587L;

    public PersistenceException(final ErrorCode errorCode, final String message) {
        super(errorCode, message);
    }
}
