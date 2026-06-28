package me.julianmartinez.pcliga.web.scraper.infrastructure.exception;

import java.io.Serial;
import java.io.Serializable;

public record ApiError(String code, String message, int status, String timestamp) implements Serializable {

    @Serial
    private static final long serialVersionUID = 777975970002621108L;

    public static ApiError create(
        final String code,
        final String message,
        final int status,
        final String timestamp
    ) {
        return new ApiError(code, message, status, timestamp);
    }

}
