package me.julianmartinez.pcliga.web.scraper.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    DUPLICATED_KEY("PCL_101", HttpStatus.CONFLICT),
    ERROR_SCRAPING("PCL_102", HttpStatus.INTERNAL_SERVER_ERROR),
    SIMULATION_TIME("PCL_201", HttpStatus.CONFLICT);

    private final String code;
    private final HttpStatus httpStatus;

    ErrorCode(final String code, final HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

}
