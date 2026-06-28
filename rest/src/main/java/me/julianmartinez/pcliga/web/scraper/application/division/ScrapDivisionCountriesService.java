package me.julianmartinez.pcliga.web.scraper.application.division;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.julianmartinez.pcliga.web.scraper.application.shared.model.exception.ScrapException;
import me.julianmartinez.pcliga.web.scraper.domain.model.DivisionDto;
import me.julianmartinez.pcliga.web.scraper.infrastructure.configuration.ScraperProperties;
import me.julianmartinez.pcliga.web.scraper.infrastructure.exception.ErrorCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapDivisionCountriesService {

    private static final String CATEGORY_SEPARATOR = "ª";

    private final WebClient webClient;
    private final ScraperProperties scraperProperties;

    public Mono<List<DivisionDto>> scrapDivisions() {

        return this.webClient.get()
            .uri(this.scraperProperties.baseUrl() + this.scraperProperties.paths().divisionStandings())
            .retrieve()
            .bodyToMono(byte[].class)
            .map(bytes -> new String(bytes, StandardCharsets.ISO_8859_1))
            .map(Jsoup::parse)
            .map(this::parseAllDivisionsFromDefaultStandingPage)
            .onErrorMap(e -> new ScrapException(ErrorCode.ERROR_SCRAPING, e.getMessage()));
    }

    private List<DivisionDto> parseAllDivisionsFromDefaultStandingPage(final Document document) {
        return document.select("option").stream()
            .map(element -> this.fromDropDownMenuElement(element.attr("value"), element.text()))
            .toList();
    }

    private DivisionDto fromDropDownMenuElement(final String id, final String fullCategoryName) {
        final var data = fullCategoryName.split(CATEGORY_SEPARATOR)[0];

        return DivisionDto.createFromDivision(
            id,
            Integer.parseInt(data.substring(data.length() - 1)),
            data.substring(0, data.length() - 2));
    }
}
