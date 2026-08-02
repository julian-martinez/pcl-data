package me.julianmartinez.pcliga.web.scraper.application;

import me.julianmartinez.pcliga.persistence.entity.SeasonState;
import me.julianmartinez.pcliga.persistence.repository.SeasonStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeasonServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Madrid");
    private static final LocalDate SEED = LocalDate.of(2026, 6, 14);

    @Mock
    private SeasonStateRepository seasonStateRepository;

    private SeasonState state;

    private SeasonService clockedService(final LocalDate today) {
        final Clock clock = Clock.fixed(today.atStartOfDay(ZONE).toInstant(), ZONE);
        return new SeasonService(this.seasonStateRepository, clock);
    }

    @BeforeEach
    void setUp() {
        this.state = new SeasonState();
        this.state.setId(1L);
        this.state.setLastResetDate(SEED);
        when(this.seasonStateRepository.findAll()).thenReturn(List.of(this.state));
    }

    @Test
    void getLastResetDate_readsFromSeasonState() {
        final SeasonService seasonService = this.clockedService(SEED);

        assertThat(seasonService.getLastResetDate()).isEqualTo(SEED);
    }

    @Test
    void getLastResetDate_throwsWhenTableIsEmpty() {
        when(this.seasonStateRepository.findAll()).thenReturn(List.of());
        final SeasonService seasonService = this.clockedService(SEED);

        assertThatIllegalStateException().isThrownBy(seasonService::getLastResetDate);
    }

    @Test
    void getNextResetDate_isSeedPlus56Days() {
        final SeasonService seasonService = this.clockedService(SEED);

        assertThat(seasonService.getNextResetDate()).isEqualTo(SEED.plusDays(56));
    }

    @ParameterizedTest(name = "day {0} after reset ({1}) -> matchday {2}")
    @CsvSource({
        // Week 1: Mon..Fri -> matchdays 1..5, Sat/Sun carry 5
        "1, MONDAY, 1",
        "2, TUESDAY, 2",
        "5, FRIDAY, 5",
        "6, SATURDAY, 5",
        "7, SUNDAY, 5",
        // Week 2 starts right after
        "8, MONDAY, 6",
        "12, FRIDAY, 10",
        // Week 7: matchdays 31..35
        "43, MONDAY, 31",
        "47, FRIDAY, 35",
        // Sunday between week 7 and week 8 carries week 7's last matchday
        "49, SUNDAY, 35",
        // Week 8: only Mon-Wed play, matchdays 36..38
        "50, MONDAY, 36",
        "51, TUESDAY, 37",
        "52, WEDNESDAY, 38",
        "53, THURSDAY, 38",
        "55, SATURDAY, 38",
        // The reset day itself (day 0/56) still shows the season's last matchday
        "0, SUNDAY, 38",
        "56, SUNDAY, 38",
    })
    void getCurrentMatchday_followsWeeklyPattern(final int daysAfterReset, final String expectedDayOfWeek, final int expectedMatchday) {
        final LocalDate today = SEED.plusDays(daysAfterReset);
        assertThat(today.getDayOfWeek().name()).isEqualTo(expectedDayOfWeek);

        final SeasonService seasonService = this.clockedService(today);

        assertThat(seasonService.getCurrentMatchday()).isEqualTo(expectedMatchday);
    }

    @Test
    void refreshLastResetDate_isNoOpBeforeNextResetIsDue() {
        final LocalDate today = SEED.plusDays(10);
        final SeasonService seasonService = this.clockedService(today);

        final LocalDate result = seasonService.refreshLastResetDate();

        assertThat(result).isEqualTo(SEED);
        verify(this.seasonStateRepository, never()).save(any());
    }

    @Test
    void refreshLastResetDate_advancesToLatestCompletedCycle() {
        // 2 full 56-day cycles plus a few days past the seed
        final LocalDate today = SEED.plusDays(56 * 2 + 5);
        final SeasonService seasonService = this.clockedService(today);

        final LocalDate result = seasonService.refreshLastResetDate();

        assertThat(result).isEqualTo(SEED.plusDays(56 * 2));
        assertThat(this.state.getLastResetDate()).isEqualTo(SEED.plusDays(56 * 2));
        verify(this.seasonStateRepository).save(this.state);
    }

    @Test
    void refreshLastResetDate_isIdempotentOnRepeatedCalls() {
        final LocalDate today = SEED.plusDays(56 + 3);
        final SeasonService seasonService = this.clockedService(today);

        seasonService.refreshLastResetDate();
        this.state.setLastResetDate(SEED.plusDays(56));
        final LocalDate second = seasonService.refreshLastResetDate();

        assertThat(second).isEqualTo(SEED.plusDays(56));
    }

    @Test
    void refreshLastResetDate_neverMovesDateBackwards() {
        // Simulate a stored date further ahead than what "today" would compute
        // (e.g. clock skew) - the stored value must win.
        this.state.setLastResetDate(SEED.plusDays(112));
        final LocalDate today = SEED.plusDays(60);
        final SeasonService seasonService = this.clockedService(today);

        final LocalDate result = seasonService.refreshLastResetDate();

        assertThat(result).isEqualTo(SEED.plusDays(112));
        verify(this.seasonStateRepository, never()).save(any());
    }

    @Test
    void refreshLastResetDate_neverReturnsAFutureDate() {
        final LocalDate today = SEED.plusDays(30);
        final SeasonService seasonService = this.clockedService(today);

        final LocalDate result = seasonService.refreshLastResetDate();

        assertThat(result).isBeforeOrEqualTo(today);
    }

}
