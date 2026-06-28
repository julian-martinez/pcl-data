package me.julianmartinez.pcliga.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import me.julianmartinez.pcliga.persistence.entity.vo.RealAverage;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(
    name = "club",
    indexes = {@Index(name = "club_url_name_idx", columnList = "url_name")},
    uniqueConstraints = {@UniqueConstraint(name = "club_url_name_uq", columnNames = {"url_name"})}
)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@Setter
public class Club implements Serializable {

    @Serial
    private static final long serialVersionUID = 3618908496413155452L;

    private static final String GENERATOR_NAME = "club_generator";

    @Id
    @SequenceGenerator(
        schema = "public",
        name = GENERATOR_NAME,
        sequenceName = "club_id_seq"
    )
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = GENERATOR_NAME
    )
    private Long id;

    @Column(name = "name", nullable = false, length = 40)
    private String name;

    @Column(name = "url_name", nullable = false, length = 40)
    private String urlName;

    @Column(name = "control", nullable = false, length = 30)
    private String control;

    @Column(name = "bot_and_has_loaned_player", columnDefinition = "boolean default false")
    private boolean botAndHasLoanedPlayer;

    @Embedded
    private RealAverage realAverage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
        value = {@JoinColumn(name = "division_id", referencedColumnName = "id")},
        foreignKey = @ForeignKey(name = "division__club_fk"))
    private Division division;

    @OneToOne(mappedBy = "club", cascade = CascadeType.ALL)
    private Standing standing;
}
