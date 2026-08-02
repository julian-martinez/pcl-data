package me.julianmartinez.pcliga.web.scraper.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DivisionDto implements Comparable<DivisionDto> {

    String internalId;
    Integer ordinal;
    String country;
    RealAverageDto realAverage;
    List<ClubDto> clubs;
    Integer inactivePlayers;
    Integer inactivePlayersWithLoanee;

    public static DivisionDto createFromDivision(final String internalId, final Integer ordinal, final String country) {
        return DivisionDto.builder()
            .internalId(internalId)
            .ordinal(ordinal)
            .country(country)
            .build();
    }

    public static DivisionDto createFromRankings(final String internalId, final String squadAverage, final String bestElevenAverage) {
        return DivisionDto.builder()
            .internalId(internalId)
            .realAverage(RealAverageDto.createFromString(squadAverage, bestElevenAverage))
            .build();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || this.getClass() != object.getClass()) return false;

        final DivisionDto that = (DivisionDto) object;
        return Objects.equals(this.internalId, that.getInternalId())
            && Objects.equals(this.ordinal, that.getOrdinal())
            && Objects.equals(this.country, that.getCountry());
    }

    @Override
    public int compareTo(final DivisionDto other) {
        if (other == null) {
            throw new NullPointerException("El valor a comparar no puede ser null");
        }

        return Comparator.comparing(DivisionDto::getCountry)
            .thenComparing(DivisionDto::getOrdinal)
            .compare(this, other);
    }
}
