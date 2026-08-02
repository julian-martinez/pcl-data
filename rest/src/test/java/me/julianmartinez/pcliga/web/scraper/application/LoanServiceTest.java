package me.julianmartinez.pcliga.web.scraper.application;

import me.julianmartinez.pcliga.persistence.entity.Club;
import me.julianmartinez.pcliga.persistence.entity.Division;
import me.julianmartinez.pcliga.persistence.repository.ClubRepository;
import me.julianmartinez.pcliga.web.scraper.domain.model.ClubDto;
import me.julianmartinez.pcliga.web.scraper.domain.model.DivisionDto;
import me.julianmartinez.pcliga.web.scraper.infrastructure.configuration.ScraperProperties;
import me.julianmartinez.pcliga.web.scraper.infrastructure.mapper.ClubMapper;
import me.julianmartinez.pcliga.web.scraper.infrastructure.mapper.DivisionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    private static final String NOT_ACTIVE_IMG = "not_active.png";
    private static final String ACTIVE_IMG = "active.png";

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private ClubMapper clubMapper;

    @Mock
    private DivisionMapper divisionMapper;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        final ScraperProperties.Selectors selectors = new ScraperProperties.Selectors(
            null, null, NOT_ACTIVE_IMG, ACTIVE_IMG, null, null
        );
        final ScraperProperties scraperProperties = new ScraperProperties(null, null, selectors);

        this.loanService = new LoanService(this.clubRepository, this.clubMapper, this.divisionMapper, scraperProperties);
    }

    @Test
    void getLoanDestinationsByCategory_populatesInactivePlayerCounts() {
        final Division division = new Division();
        division.setId(1L);
        division.setInternalId("D1");
        division.setOrdinal("1");

        final Club inactiveWithLoanee = this.buildClub(1L, NOT_ACTIVE_IMG, true, division);
        final Club inactiveWithoutLoanee = this.buildClub(2L, NOT_ACTIVE_IMG, false, division);
        final Club activeClub = this.buildClub(3L, ACTIVE_IMG, true, division);

        when(this.clubRepository.findByDivisionOrdinalAndControl("1", NOT_ACTIVE_IMG))
            .thenReturn(List.of(inactiveWithLoanee, inactiveWithoutLoanee, activeClub));

        when(this.clubMapper.toClubDto(any(Club.class)))
            .thenAnswer(invocation -> {
                final Club club = invocation.getArgument(0);
                return ClubDto.builder()
                    .urlName("club-" + club.getId())
                    .control(club.getControl())
                    .botAndHasLoanedPlayer(club.isBotAndHasLoanedPlayer())
                    .build();
            });

        when(this.divisionMapper.toDivisionDto(division))
            .thenReturn(DivisionDto.createFromDivision("D1", 1, "SPAIN"));

        final List<DivisionDto> result = this.loanService.getLoanDestinationsByCategory(1);

        assertThat(result).hasSize(1);
        final DivisionDto divisionDto = result.getFirst();
        assertThat(divisionDto.getInactivePlayers()).isEqualTo(2);
        assertThat(divisionDto.getInactivePlayersWithLoanee()).isEqualTo(1);
        assertThat(divisionDto.getClubs()).hasSize(3);
    }

    private Club buildClub(final Long id, final String control, final boolean botAndHasLoanedPlayer, final Division division) {
        final Club club = new Club();
        club.setId(id);
        club.setName("Club " + id);
        club.setUrlName("club-" + id);
        club.setControl(control);
        club.setBotAndHasLoanedPlayer(botAndHasLoanedPlayer);
        club.setDivision(division);
        return club;
    }

}
