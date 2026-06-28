package me.julianmartinez.pcliga.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import me.julianmartinez.pcliga.persistence.entity.vo.RealAverage;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(
    name = "division",
    indexes = {
        @Index(name = "division_internal_id_idx", columnList = "internal_id"),
        @Index(name = "division_ordinal_idx", columnList = "ordinal")
    },
    uniqueConstraints = {@UniqueConstraint(name = "division_internal_id_uq", columnNames = {"internal_id"})}
)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Setter
public class Division implements Serializable {

    @Serial
    private static final long serialVersionUID = 2784438178034726538L;

    private static final String GENERATOR_NAME = "division_generator";

    @Id
    @SequenceGenerator(
        schema = "public",
        name = GENERATOR_NAME,
        sequenceName = "division_id_seq"
    )
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = GENERATOR_NAME
    )
    private Long id;

    @Column(name = "internal_id", nullable = false, length = 5)
    private String internalId;

    @Column(name = "ordinal", nullable = false, length = 1)
    private String ordinal;

    @Embedded
    private RealAverage realAverage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
        value = {@JoinColumn(name = "country_id", referencedColumnName = "id")},
        foreignKey = @ForeignKey(name = "country__division_fk"))
    private Country country;
}
