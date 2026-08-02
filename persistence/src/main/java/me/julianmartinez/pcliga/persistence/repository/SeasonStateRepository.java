package me.julianmartinez.pcliga.persistence.repository;

import me.julianmartinez.pcliga.persistence.entity.SeasonState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonStateRepository extends JpaRepository<SeasonState, Long> {
}
