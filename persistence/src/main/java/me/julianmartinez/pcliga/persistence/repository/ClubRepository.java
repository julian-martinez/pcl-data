package me.julianmartinez.pcliga.persistence.repository;

import me.julianmartinez.pcliga.persistence.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    @Query("""
            SELECT DISTINCT c
            FROM Club c
            JOIN FETCH c.division d
            JOIN FETCH d.country co
            WHERE d.ordinal = :ordinal
            AND c.control = :control
        """)
    List<Club> findByDivisionOrdinalAndControl(@Param("ordinal") String ordinal, @Param("control") String control);

    Club findByUrlName(final String urlName);

    List<Club> findByUrlNameIn(Collection<String> urlNames);

}
