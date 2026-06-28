package me.julianmartinez.pcliga.persistence.repository;

import me.julianmartinez.pcliga.persistence.entity.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Long> {

    List<Division> findAllByInternalIdIn(Collection<String> internalIds);

    List<Division> findByOrdinal(String ordinal);

}
