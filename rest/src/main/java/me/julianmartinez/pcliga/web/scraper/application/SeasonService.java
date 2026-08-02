package me.julianmartinez.pcliga.web.scraper.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.julianmartinez.pcliga.persistence.entity.SeasonState;
import me.julianmartinez.pcliga.persistence.repository.SeasonStateRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Tracks the league's rolling 56-day season cycle: when it last reset, which
 * matchday (jornada) is currently in play, and when the next reset is due.
 * <p>
 * All state lives in the single-row {@code season_state} table (seeded by
 * Liquibase); this service never reads the anchor date from configuration.
 * <p>
 * Weekly pattern, counted from the day after {@code last_reset_date} (a
 * Sunday): Monday-Friday play one matchday each (5/week) for 7 weeks (35
 * matchdays), then an 8th week plays only Monday-Wednesday (matchdays 36-38).
 * Saturdays and Sundays never have a league matchday; on those days {@link
 * #getCurrentMatchday()} keeps returning the last matchday played that cycle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonService {

    /**
     * The literal historical reset anchor seeded into {@code season_state} by
     * the PCL-4 Liquibase changelog. {@code refreshLastResetDate()} always
     * recomputes from this fixed point, never from the currently stored
     * value, so the calculation stays idempotent no matter how many cycles
     * have elapsed since the last refresh.
     */
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
        final long cyclePosition = Math.floorMod(daysSinceReset, RESET_CYCLE_DAYS); // 0..55

        // Re-index to 1..56 so the Sunday that closes a cycle (remainder 0)
        // is treated as day 56 of the *current* cycle rather than day 0 of
        // the next one - this keeps week boundaries (e.g. the Sunday between
        // week 7 and week 8) from being misclassified.
        final long dayOfCycle = Math.floorMod(cyclePosition - 1, RESET_CYCLE_DAYS) + 1; // 1..56

        final int weekIndex = (int) ((dayOfCycle - 1) / 7); // 0..7
        final int dayInWeek = (int) ((dayOfCycle - 1) % 7) + 1; // 1=Mon .. 7=Sun

        if (weekIndex < REGULAR_WEEKS) {
            if (dayInWeek <= MATCHDAYS_PER_REGULAR_WEEK) {
                return weekIndex * MATCHDAYS_PER_REGULAR_WEEK + dayInWeek;
            }
            // Saturday/Sunday: carry the last matchday played this week
            return weekIndex * MATCHDAYS_PER_REGULAR_WEEK + MATCHDAYS_PER_REGULAR_WEEK;
        }

        // Week 8: only Monday-Wednesday play, matchdays 36-38
        if (dayInWeek <= 3) {
            return REGULAR_WEEKS * MATCHDAYS_PER_REGULAR_WEEK + dayInWeek;
        }
        // Thursday through Sunday: season is done for the cycle, holds at 38
        return TOTAL_MATCHDAYS;
    }

    /**
     * Recomputes {@code last_reset_date} as
     * {@code SEED_RESET_DATE + floor((today - SEED_RESET_DATE) / 56) * 56}
     * and persists it. Idempotent (repeated calls on the same day are a
     * no-op) and monotonic: the stored date is never moved backwards nor set
     * to a future date, regardless of clock skew.
     */
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
