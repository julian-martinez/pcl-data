package me.julianmartinez.pcliga.web.scraper.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scraper")
public record ScraperProperties(
    String baseUrl,
    Paths paths,
    Selectors selectors
) {

    public record Paths(
        String divisionStandings,
        String clubStandings,
        String clubRankings,
        String divisionRankings
    ) {}

    public record Selectors(
        String tableRows,
        String botLoanFlag,
        String teamNotActiveImg,
        String teamActiveImg,
        String teamVacationImg,
        String teamPendingValidationImg
    ) {}
}
