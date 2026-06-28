package me.julianmartinez.pcliga.web.scraper.infrastructure.configuration;

import com.google.common.net.HttpHeaders;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;


@Configuration
@RequiredArgsConstructor
public class WebClientConfiguration {

    private final ScraperProperties scraperProperties;

    @Bean
    public WebClient webClient() {

        final ConnectionProvider connectionProvider = ConnectionProvider.builder("scraping-pool")
            .maxConnections(10)
            .pendingAcquireMaxCount(1000)
            .pendingAcquireTimeout(Duration.ofSeconds(30))
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofMinutes(5))
            .evictInBackground(Duration.ofSeconds(30))
            .build();

        final HttpClient httpClient = HttpClient.create(connectionProvider)
            .responseTimeout(Duration.ofSeconds(15))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .followRedirect(true)
            .compress(true)
            .keepAlive(true);

        final ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();

        return WebClient.builder()
            .defaultHeader(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            .defaultHeader(HttpHeaders.ACCEPT,
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "es-ES,es;q=0.9,en;q=0.8")
            .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br")
            .defaultHeader(HttpHeaders.CONNECTION, "keep-alive")
            .defaultHeader(HttpHeaders.REFERER, scraperProperties.baseUrl() + "/")
            .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .build();
    }
}
