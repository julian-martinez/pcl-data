package me.julianmartinez.pcliga.web.scraper.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfiguration {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Madrid"));
    }

}
