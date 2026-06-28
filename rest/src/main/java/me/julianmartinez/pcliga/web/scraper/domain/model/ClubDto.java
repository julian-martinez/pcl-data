package me.julianmartinez.pcliga.web.scraper.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ClubDto {

    String teamName;
    String urlName;
    String control;
    boolean botAndHasLoanedPlayer;
    RealAverageDto realAverage;

    public static ClubDto createFromRanking(
        final String urlName,
        final String squadAverage,
        final String bestElevenAverage
    ) {
        return ClubDto.builder()
            .urlName(urlName)
            .realAverage(RealAverageDto.createFromString(squadAverage, bestElevenAverage))
            .build();
    }
}
