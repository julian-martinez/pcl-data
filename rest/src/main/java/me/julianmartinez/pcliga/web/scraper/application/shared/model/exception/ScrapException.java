package me.julianmartinez.pcliga.web.scraper.application.shared.model.exception;

import me.julianmartinez.pcliga.web.scraper.infrastructure.exception.ErrorCode;

import java.io.Serial;

public class ScrapException extends AppException {

    @Serial
    private static final long serialVersionUID = 3821061504269822357L;

    public ScrapException(final ErrorCode errorCode, final String message) {
        super(errorCode, message);
    }
}
