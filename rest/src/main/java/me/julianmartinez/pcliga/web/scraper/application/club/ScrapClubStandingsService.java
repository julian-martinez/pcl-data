package me.julianmartinez.pcliga.web.scraper.application.club;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.julianmartinez.pcliga.web.scraper.application.shared.model.exception.ScrapException;
import me.julianmartinez.pcliga.web.scraper.domain.model.ClubDto;
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
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapClubStandingsService {

    public static final String BOT_HAS_PLAYER_ON_LOAN_FLAG = "C";

    private final WebClient webClient;
    private final ScraperProperties scraperProperties;

    public Mono<List<ClubDto>> parseClubs(final String divisionId) {
        return this.fetchHtml(this.scraperProperties.baseUrl() + this.scraperProperties.paths().clubStandings() + divisionId)
            .retryWhen(Retry.backoff(2, Duration.ofSeconds(2)).maxBackoff(Duration.ofSeconds(10)))
            .map(this::parseHtml)
            .onErrorMap(e -> new ScrapException(ErrorCode.ERROR_SCRAPING, e.getMessage()));
    }

    public Mono<String> fetchHtml(final String url) {
        final long start = System.currentTimeMillis();

        return this.webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(byte[].class)
            .map(bytes -> new String(bytes, StandardCharsets.ISO_8859_1))
            .doOnSuccess(html -> log.info("OK {} in {} ms", url, System.currentTimeMillis() - start))
            .doOnError(error -> log.error("ERROR {} after {} ms", url, System.currentTimeMillis() - start, error));
    }

    private List<ClubDto> parseHtml(final String html) {
        final Document doc = Jsoup.parse(html);
        final Elements rows = doc.select(this.scraperProperties.selectors().tableRows());

        return rows.stream()
            .skip(1)
            .map(this::fromTableRowElement)
            .toList();
    }

    private ClubDto fromTableRowElement(final Element row) {
        final Elements columns = row.select("td");

        return ClubDto.builder()
            .teamName(columns.get(3).text().trim())
            .urlName(this.urlName(columns.get(0)))
            .control(this.control(columns.get(1)))
            .botAndHasLoanedPlayer(this.botHasPlayerOnLoan(columns.get(1)))
            .build();
    }

    private String control(final Element column) {
        final Element img = column.selectFirst("img");
        return img != null ? img.attr("src") : StringUtils.EMPTY;
    }

    private boolean botHasPlayerOnLoan(final Element column) {
        final Element flag = column.selectFirst(this.scraperProperties.selectors().botLoanFlag());
        return flag != null && BOT_HAS_PLAYER_ON_LOAN_FLAG.equals(flag.text());
    }

    private String urlName(final Element column) {
        final Element anchor = column.select("a").get(1);
        return anchor != null ? anchor.attr("href").split("equipo=")[1].split("&")[0] : StringUtils.EMPTY;
    }
}
