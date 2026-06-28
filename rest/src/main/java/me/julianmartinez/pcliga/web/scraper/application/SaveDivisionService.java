package me.julianmartinez.pcliga.web.scraper.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.julianmartinez.pcliga.persistence.repository.CountryRepository;
import me.julianmartinez.pcliga.web.scraper.application.division.ScrapDivisionCountriesService;
import me.julianmartinez.pcliga.web.scraper.application.shared.model.exception.PersistenceException;
import me.julianmartinez.pcliga.web.scraper.domain.model.DivisionDto;
import me.julianmartinez.pcliga.web.scraper.domain.model.SeedResult;
import me.julianmartinez.pcliga.web.scraper.infrastructure.exception.ErrorCode;
import me.julianmartinez.pcliga.web.scraper.infrastructure.mapper.CountryMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaveDivisionService {

    private final ScrapDivisionCountriesService scrapDivisionCountriesService;
    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    public Mono<SeedResult> saveCountriesAndDivisions() {
        return this.scrapDivisionCountriesService.scrapDivisions()
            .flatMap(this::filterAndSave);
    }

    private Mono<SeedResult> filterAndSave(final List<DivisionDto> divisions) {
        return Mono.fromCallable(() -> {

                final Set<String> existingCountryNames = this.countryRepository.findAll()
                    .stream()
                    .map(country -> country.getName().getValue())
                    .collect(Collectors.toSet());

                final List<DivisionDto> newDivisions = divisions.stream()
                    .filter(divisionDto -> !existingCountryNames.contains(divisionDto.getCountry()))
                    .toList();

                this.countryRepository.saveAll(this.countryMapper.toCountryList(newDivisions));

                final int skipped = (int) divisions.stream()
                    .map(DivisionDto::getCountry)
                    .distinct()
                    .filter(existingCountryNames::contains)
                    .count();

                final int created = (int) newDivisions.stream()
                    .map(DivisionDto::getCountry)
                    .distinct()
                    .count();

                log.info("Save divisions: {} new countries created, {} already existed", created, skipped);

                return new SeedResult(created, skipped);
            })
            .onErrorMap(DataIntegrityViolationException.class, e -> new PersistenceException(ErrorCode.DUPLICATED_KEY, e.getMessage()))
            .subscribeOn(Schedulers.boundedElastic());
    }
}
