package me.julianmartinez.pcliga.web.scraper.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CountryDto {

    String name;
    List<DivisionDto> divisions;
}
