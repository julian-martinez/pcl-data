package me.julianmartinez.pcliga.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "season_state")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Setter
public class SeasonState implements Serializable {

    @Serial
    private static final long serialVersionUID = 5726913840871142365L;

    @Id
    private Long id;

    @Column(name = "last_reset_date", nullable = false)
    private LocalDate lastResetDate;

}
