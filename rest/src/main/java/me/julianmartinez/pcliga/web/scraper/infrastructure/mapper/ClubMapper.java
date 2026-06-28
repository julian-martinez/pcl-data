package me.julianmartinez.pcliga.web.scraper.infrastructure.mapper;

import me.julianmartinez.pcliga.persistence.entity.Club;
import me.julianmartinez.pcliga.web.scraper.domain.model.ClubDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
    componentModel = SPRING,
    injectionStrategy = CONSTRUCTOR,
    uses = {
        RealAverageMapper.class
    }
)
public interface ClubMapper {

    @Mappings(value = {
        @Mapping(target = "teamName", source = "name"),
    })
    ClubDto toClubDto(final Club club);

    @Mappings(value = {
        @Mapping(target = "name", source = "teamName"),
    })
    Club toClubEntity(final ClubDto clubDto);
}
