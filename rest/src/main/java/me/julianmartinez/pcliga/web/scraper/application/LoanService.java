package me.julianmartinez.pcliga.web.scraper.application;

import lombok.RequiredArgsConstructor;
import me.julianmartinez.pcliga.persistence.entity.Club;
import me.julianmartinez.pcliga.persistence.repository.ClubRepository;
import me.julianmartinez.pcliga.web.scraper.domain.model.ClubDto;
import me.julianmartinez.pcliga.web.scraper.domain.model.DivisionDto;
import me.julianmartinez.pcliga.web.scraper.infrastructure.configuration.ScraperProperties;
import me.julianmartinez.pcliga.web.scraper.infrastructure.mapper.ClubMapper;
import me.julianmartinez.pcliga.web.scraper.infrastructure.mapper.DivisionMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;
    private final DivisionMapper divisionMapper;
    private final ScraperProperties scraperProperties;

    public List<DivisionDto> getLoanDestinationsByCategory(final Integer category) {
        final String teamNotActiveImg = this.scraperProperties.selectors().teamNotActiveImg();

        return this.clubRepository.findByDivisionOrdinalAndControl(String.valueOf(category), teamNotActiveImg)
            .stream()
            .collect(Collectors.groupingBy(club -> club.getDivision().getInternalId()))
            .values().stream()
            .map(clubs -> this.buildDivisionDto(clubs, teamNotActiveImg))
            .sorted()
            .toList();
    }

    private DivisionDto buildDivisionDto(final List<Club> clubs, final String teamNotActiveImg) {
        final List<ClubDto> clubDtos = clubs.stream()
            .map(this.clubMapper::toClubDto)
            .toList();

        return this.divisionMapper.toDivisionDto(clubs.getFirst().getDivision()).toBuilder()
            .clubs(clubDtos)
            .inactivePlayers(this.countInactivePlayers(clubs, teamNotActiveImg))
            .inactivePlayersWithLoanee(this.countInactivePlayersWithLoanee(clubs, teamNotActiveImg))
            .build();
    }

    private Integer countInactivePlayers(final List<Club> clubs, final String teamNotActiveImg) {
        return Math.toIntExact(clubs.stream()
            .filter(club -> teamNotActiveImg.equals(club.getControl()))
            .count());
    }

    private Integer countInactivePlayersWithLoanee(final List<Club> clubs, final String teamNotActiveImg) {
        return Math.toIntExact(clubs.stream()
            .filter(club -> teamNotActiveImg.equals(club.getControl()))
            .filter(Club::isBotAndHasLoanedPlayer)
            .count());
    }

}
