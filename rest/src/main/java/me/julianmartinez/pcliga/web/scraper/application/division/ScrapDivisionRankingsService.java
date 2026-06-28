package me.julianmartinez.pcliga.web.scraper.application.division;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.julianmartinez.pcliga.web.scraper.application.shared.model.exception.ScrapException;
import me.julianmartinez.pcliga.web.scraper.domain.model.DivisionDto;
import me.julianmartinez.pcliga.web.scraper.infrastructure.configuration.ScraperProperties;
import me.julianmartinez.pcliga.web.scraper.infrastructure.exception.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapDivisionRankingsService {

    private final WebClient webClient;
    private final ScraperProperties scraperProperties;

    public Mono<List<DivisionDto>> parseRankings() {

        return this.webClient.get()
            .uri(scraperProperties.baseUrl() + scraperProperties.paths().divisionRankings())
            .retrieve()
            .bodyToMono(byte[].class)
            .map(bytes -> new String(bytes, StandardCharsets.ISO_8859_1))
            .map(Jsoup::parse)
            .map(this::parseDivisionRankings)
            .onErrorMap(e -> new ScrapException(ErrorCode.ERROR_SCRAPING, e.getMessage()));
    }

    private List<DivisionDto> parseDivisionRankings(final Document document) {
        final Elements rows = document.select(scraperProperties.selectors().tableRows());

        return rows.stream()
            .skip(1)
            .map(this::fromTableRowElement)
            .toList();
    }

    private DivisionDto fromTableRowElement(final Element row) {
        final Elements columns = row.select("td");

        return DivisionDto.createFromRankings(
            this.divisionId(columns.get(2)),
            this.replaceDecimalPoint(columns.get(3)),
            this.replaceDecimalPoint(columns.get(4))
        );
    }

    private String divisionId(final Element column) {
        final Element anchor = column.select("a").get(0);
        return anchor != null ? anchor.attr("href").split("grupo=")[1] : StringUtils.EMPTY;
    }

    private String replaceDecimalPoint(final Element column) {
        final String value = column.text();
        return value.trim().replace(",", ".");
    }
}
