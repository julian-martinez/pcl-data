package me.julianmartinez.pcliga.web.scraper.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Exposes the wall clock used for date-dependent business logic (season
 * resets, matchday calculation). Pinned to Spain's timezone since the target
 * league's reset and matchday schedule is defined in local Spanish time,
 * regardless of the server's own system timezone.
 */
@Configuration
public class TimeConfiguration {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Madrid"));
    }

}
