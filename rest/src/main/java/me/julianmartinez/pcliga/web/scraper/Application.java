package me.julianmartinez.pcliga.web.scraper;

import me.julianmartinez.pcliga.persistence.configuration.JpaConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(JpaConfiguration.class)
@ConfigurationPropertiesScan
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
