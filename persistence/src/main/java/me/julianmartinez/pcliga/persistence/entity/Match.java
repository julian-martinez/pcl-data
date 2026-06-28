package me.julianmartinez.pcliga.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "match")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Setter
public class Match {

    private static final String GENERATOR_NAME = "match_generator";

    @Id
    @SequenceGenerator(
        schema = "public",
        name = GENERATOR_NAME,
        sequenceName = "match_id_seq"
    )
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = GENERATOR_NAME
    )
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns(
        value = {@JoinColumn(name = "home_club_id", referencedColumnName = "id")},
        foreignKey = @ForeignKey(name = "home__match_fk"))
    private Club home;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns(
        value = {@JoinColumn(name = "away_club_id", referencedColumnName = "id")},
        foreignKey = @ForeignKey(name = "away__match_fk"))
    private Club away;

    @Column(name = "match_day")
    private int matchDay;

    @Column(name = "date")
    private LocalDate date;

}
