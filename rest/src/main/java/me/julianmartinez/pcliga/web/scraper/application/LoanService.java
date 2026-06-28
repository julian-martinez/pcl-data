package me.julianmartinez.pcliga.web.scraper.application;

import lombok.RequiredArgsConstructor;
import me.julianmartinez.pcliga.persistence.repository.ClubRepository;
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
        return this.clubRepository.findByDivisionOrdinalAndControl(String.valueOf(category), this.scraperProperties.selectors().teamNotActiveImg())
            .stream()
            .collect(Collectors.groupingBy(club -> club.getDivision().getInternalId()))
            .values().stream()
            .map(clubs -> this.divisionMapper.toDivisionDto(clubs.getFirst().getDivision()).toBuilder()
                .clubs(clubs.stream()
                    .map(this.clubMapper::toClubDto)
                    .toList())
                .build())
            .sorted()
            .toList();
    }

}
