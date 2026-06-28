package me.julianmartinez.pcliga.web.scraper.domain.model;

import java.math.BigDecimal;

public record RealAverageDto(BigDecimal squadAverage, BigDecimal bestElevenAverage) {

    public static RealAverageDto createFromString(final String squad, final String bestEleven) {
        return new RealAverageDto(
            new BigDecimal(squad),
            new BigDecimal(bestEleven)
        );
    }
}
