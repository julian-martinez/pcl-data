package me.julianmartinez.pcliga.web.scraper.infrastructure.mapper;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;

public interface RelationshipMapper {

    default <P, C> void linkOneToOne(final C child, final P parent, final BiConsumer<C, P> setter) {
        if (nonNull(child)) {
            setter.accept(child, parent);
        }
    }

    default <P, C> void linkManyToOne(final List<C> children, final P parent, final BiConsumer<C, P> setter) {
        if (nonNull(children)) {
            children.forEach(child -> setter.accept(child, parent));
        }
    }
}
