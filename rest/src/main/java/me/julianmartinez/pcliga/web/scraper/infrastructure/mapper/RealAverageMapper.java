package me.julianmartinez.pcliga.web.scraper.infrastructure.mapper;

import me.julianmartinez.pcliga.persistence.entity.vo.RealAverage;
import me.julianmartinez.pcliga.web.scraper.domain.model.RealAverageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
    componentModel = SPRING,
    injectionStrategy = CONSTRUCTOR,
    uses = {}
)
public interface RealAverageMapper {

    @Mappings(value = {})
    RealAverageDto toRealAverageDto(final RealAverage realAverage);

    @Mappings(value = {})
    RealAverage toRealAverageVo(final RealAverageDto realAverageDto);
}
