package me.julianmartinez.pcliga.web.scraper.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.julianmartinez.pcliga.persistence.entity.Club;
import me.julianmartinez.pcliga.persistence.entity.Division;
import me.julianmartinez.pcliga.persistence.repository.ClubRepository;
import me.julianmartinez.pcliga.persistence.repository.DivisionRepository;
import me.julianmartinez.pcliga.web.scraper.application.club.ScrapClubRankingsService;
import me.julianmartinez.pcliga.web.scraper.application.club.ScrapClubStandingsService;
import me.julianmartinez.pcliga.web.scraper.application.division.ScrapDivisionRankingsService;
import me.julianmartinez.pcliga.web.scraper.application.shared.model.exception.PersistenceException;
import me.julianmartinez.pcliga.web.scraper.application.shared.model.exception.ScrapException;
import me.julianmartinez.pcliga.web.scraper.domain.model.ClubDto;
import me.julianmartinez.pcliga.web.scraper.domain.model.DivisionDto;
import me.julianmartinez.pcliga.web.scraper.infrastructure.exception.ErrorCode;
import me.julianmartinez.pcliga.web.scraper.infrastructure.mapper.RealAverageMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaveClubService {

    private static final int CONCURRENCY = 2;
    private static final Duration DELAY_BETWEEN_DIVISIONS = Duration.ofMillis(500);

    private final ScrapClubStandingsService scrapClubStandingsService;
    private final ScrapClubRankingsService scrapClubRankingsService;
    private final ScrapDivisionRankingsService scrapDivisionRankingsService;
    private final ClubRepository clubRepository;
    private final DivisionRepository divisionRepository;
    private final RealAverageMapper realAverageMapper;

    public Flux<String> updateAllClubData() {
        return Mono.fromCallable(this.divisionRepository::findAll)
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany(this::processDivisions);
    }

    public Flux<String> updateClubDataByCategory(final Integer category) {
        return Mono.fromCallable(() -> this.divisionRepository.findByOrdinal(String.valueOf(category)))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany(this::processDivisions);
    }

    private Flux<String> processDivisions(final List<Division> divisions) {
        final Set<String> dbInternalIds = divisions.stream()
            .map(Division::getInternalId)
            .collect(Collectors.toSet());

        final Flux<String> clubUpdates = Flux.fromIterable(divisions)
            .delayElements(DELAY_BETWEEN_DIVISIONS)
            .flatMap(this::processAndSaveDivision, CONCURRENCY);

        final Flux<String> realAverageDivisionUpdates = this.updateDivisionRealAverage(dbInternalIds)
            .thenMany(Flux.<String>empty());

        return Flux.merge(clubUpdates, realAverageDivisionUpdates);
    }

    private Flux<String> processAndSaveDivision(final Division division) {
        return Mono.zip(
                this.scrapClubStandingsService.parseClubs(division.getInternalId()),
                this.scrapClubRankingsService.parseRankings(division.getInternalId())
            )
            .flatMap(tuple -> this.mergeClubData(tuple.getT1(), tuple.getT2()))
            .flatMap(clubs -> this.saveClubs(division, clubs))
            .doOnSuccess(clubs -> log.info("Division {} saved: {} clubs", division.getInternalId(), clubs.size()))
            .flatMapMany(Flux::fromIterable)
            .map(ClubDto::getUrlName)
            .onErrorResume(e -> {
                log.error("Division {} discarded: {}", division.getInternalId(), e.getMessage());
                return Flux.empty();
            });
    }

    private Mono<List<ClubDto>> mergeClubData(final List<ClubDto> clubs, final List<ClubDto> rankings) {
        return Flux.concat(Flux.fromIterable(clubs), Flux.fromIterable(rankings))
            .groupBy(ClubDto::getUrlName)
            .flatMap(group -> group.reduce(this::merge))
            .filter(club -> nonNull(club.getTeamName()) && nonNull(club.getRealAverage()))
            .collectList();
    }

    private Mono<List<ClubDto>> saveClubs(final Division division, final List<ClubDto> clubs) {
        return Mono.fromCallable(() -> {
                final List<String> urlNames = clubs.stream().map(ClubDto::getUrlName).toList();
                final Map<String, Club> existing = this.clubRepository.findByUrlNameIn(urlNames).stream()
                    .collect(Collectors.toMap(Club::getUrlName, club -> club));

                final List<Club> toSave = clubs.stream()
                    .map(dto -> {
                        final Club club = existing.getOrDefault(dto.getUrlName(), new Club());
                        club.setName(dto.getTeamName());
                        club.setUrlName(dto.getUrlName());
                        club.setControl(dto.getControl());
                        club.setBotAndHasLoanedPlayer(dto.isBotAndHasLoanedPlayer());
                        club.setRealAverage(this.realAverageMapper.toRealAverageVo(dto.getRealAverage()));
                        club.setDivision(division);
                        return club;
                    })
                    .toList();

                this.clubRepository.saveAll(toSave);
                return clubs;
            })
            .onErrorMap(DataIntegrityViolationException.class, e -> new PersistenceException(ErrorCode.DUPLICATED_KEY, e.getMessage()))
            .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Void> updateDivisionRealAverage(final Set<String> dbInternalIds) {
        return this.scrapDivisionRankingsService.parseRankings()
            .onErrorResume(ScrapException.class, e -> {
                log.error("Discarded division average update: {}", e.getMessage());
                return Mono.empty();
            })
            .flatMap(dtos -> Mono.fromCallable(() -> {
                final List<DivisionDto> matching = dtos.stream()
                    .filter(dto -> dbInternalIds.contains(dto.getInternalId()) && nonNull(dto.getRealAverage()))
                    .toList();

                final Map<String, Division> byInternalId = this.divisionRepository
                    .findAllByInternalIdIn(matching.stream().map(DivisionDto::getInternalId).toList())
                    .stream()
                    .collect(Collectors.toMap(Division::getInternalId, div -> div));

                final List<Division> toSave = new ArrayList<>();
                for (final DivisionDto dto : matching) {
                    final Division division = byInternalId.get(dto.getInternalId());
                    if (division != null) {
                        division.setRealAverage(this.realAverageMapper.toRealAverageVo(dto.getRealAverage()));
                        toSave.add(division);
                    }
                }

                this.divisionRepository.saveAll(toSave);
                log.info("Division averages updated: {}", toSave.size());
                return null;
            }).subscribeOn(Schedulers.boundedElastic()))
            .then();
    }

    private ClubDto merge(final ClubDto first, final ClubDto second) {
        final ClubDto base = nonNull(first.getTeamName()) ? first : second;
        final ClubDto average = nonNull(first.getRealAverage()) ? first : second;

        return ClubDto.builder()
            .urlName(base.getUrlName())
            .teamName(base.getTeamName())
            .control(base.getControl())
            .botAndHasLoanedPlayer(base.isBotAndHasLoanedPlayer())
            .realAverage(average.getRealAverage())
            .build();
    }
}
