package me.julianmartinez.pcliga.web.scraper.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.julianmartinez.pcliga.persistence.entity.SeasonState;
import me.julianmartinez.pcliga.persistence.repository.SeasonStateRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonService {

    static final LocalDate SEED_RESET_DATE = LocalDate.of(2026, 6, 14);

    static final int RESET_CYCLE_DAYS = 56;

    private static final int MATCHDAYS_PER_REGULAR_WEEK = 5;
    private static final int REGULAR_WEEKS = 7;
    private static final int TOTAL_MATCHDAYS = 38;

    private final SeasonStateRepository seasonStateRepository;
    private final Clock clock;

    public LocalDate getLastResetDate() {
        return this.loadState().getLastResetDate();
    }

    public LocalDate getNextResetDate() {
        return this.getLastResetDate().plusDays(RESET_CYCLE_DAYS);
    }

    public int getCurrentMatchday() {
        final LocalDate lastResetDate = this.getLastResetDate();
        final LocalDate today = LocalDate.now(this.clock);

        final long daysSinceReset = ChronoUnit.DAYS.between(lastResetDate, today);
        final long cyclePosition = Math.floorMod(daysSinceReset, RESET_CYCLE_DAYS);

        final long dayOfCycle = Math.floorMod(cyclePosition - 1, RESET_CYCLE_DAYS) + 1;

        final int weekIndex = (int) ((dayOfCycle - 1) / 7);
        final int dayInWeek = (int) ((dayOfCycle - 1) % 7) + 1;

        if (weekIndex < REGULAR_WEEKS) {
            if (dayInWeek <= MATCHDAYS_PER_REGULAR_WEEK) {
                return weekIndex * MATCHDAYS_PER_REGULAR_WEEK + dayInWeek;
            }
            return weekIndex * MATCHDAYS_PER_REGULAR_WEEK + MATCHDAYS_PER_REGULAR_WEEK;
        }

        if (dayInWeek <= 3) {
            return REGULAR_WEEKS * MATCHDAYS_PER_REGULAR_WEEK + dayInWeek;
        }
        return TOTAL_MATCHDAYS;
    }

    public LocalDate refreshLastResetDate() {
        final LocalDate today = LocalDate.now(this.clock);
        final long daysSinceSeed = ChronoUnit.DAYS.between(SEED_RESET_DATE, today);
        final long elapsedCycles = Math.floorDiv(daysSinceSeed, RESET_CYCLE_DAYS);
        final LocalDate computedResetDate = SEED_RESET_DATE.plusDays(elapsedCycles * RESET_CYCLE_DAYS);

        final SeasonState state = this.loadState();
        final LocalDate currentResetDate = state.getLastResetDate();

        final LocalDate updatedResetDate = computedResetDate.isAfter(currentResetDate)
            ? computedResetDate
            : currentResetDate;

        if (!updatedResetDate.isEqual(currentResetDate)) {
            state.setLastResetDate(updatedResetDate);
            this.seasonStateRepository.save(state);
            log.info("Season reset date refreshed: {} -> {}", currentResetDate, updatedResetDate);
        }

        return updatedResetDate;
    }

    private SeasonState loadState() {
        return this.seasonStateRepository.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "season_state table has no row - check that the PCL-4 Liquibase changelog has been applied"));
    }

}
