package me.julianmartinez.pcliga.web.scraper.infrastructure.mapper;

import me.julianmartinez.pcliga.persistence.entity.Country;
import me.julianmartinez.pcliga.web.scraper.domain.model.CountryDto;
import me.julianmartinez.pcliga.web.scraper.domain.model.DivisionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
    componentModel = SPRING,
    injectionStrategy = CONSTRUCTOR,
    uses = {
        DivisionMapper.class,
    }
)
public interface CountryMapper {

    @Mappings(value = {
        @Mapping(target = "name", source = "name", qualifiedByName = "toCountryNameEnum")
    })
    Country toCountryEntity(final CountryDto countryDto);

    @Named("toCountryNameEnum")
    default Country.CountryName toCountryNameEnum(final String country) {
        return Country.CountryName.fromString(country);
    }

    default List<Country> toCountryList(final List<DivisionDto> divisionDtoList) {

        return divisionDtoList.stream()
            .collect(Collectors.groupingBy(DivisionDto::getCountry))
            .entrySet()
            .stream()
            .map(entry -> CountryDto.builder()
                .name(entry.getKey())
                .divisions(entry.getValue())
                .build())
            .map(this::toCountryEntity)
            .toList();
    }
}
