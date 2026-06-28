package me.julianmartinez.pcliga.web.scraper.infrastructure.mapper;

import me.julianmartinez.pcliga.persistence.entity.Country;
import me.julianmartinez.pcliga.persistence.entity.Division;
import me.julianmartinez.pcliga.web.scraper.domain.model.DivisionDto;
import org.mapstruct.*;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
    componentModel = SPRING,
    injectionStrategy = CONSTRUCTOR,
    uses = {
        RealAverageMapper.class,
    }
)
public interface DivisionMapper extends RelationshipMapper {

    @Mapping(target = "country", source = "country.name")
    DivisionDto toDivisionDto(final Division division);

    @Mappings(value = {
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "country", ignore = true)
    })
    Division toDivisionEntity(final DivisionDto divisionDto);

    @AfterMapping
    default void linkRelationship(@MappingTarget final Country country) {
        this.linkManyToOne(country.getDivisions(), country, Division::setCountry);
    }

    default String mapCountryName(final Country.CountryName name) {
        return name != null ? name.name() : null;
    }

}
