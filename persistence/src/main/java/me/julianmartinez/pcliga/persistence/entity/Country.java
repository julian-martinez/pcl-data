package me.julianmartinez.pcliga.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(
    name = "country",
    indexes = {@Index(name = "country_name_idx", columnList = "name")},
    uniqueConstraints = {@UniqueConstraint(name = "country_name_uq", columnNames = {"name"})}
)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Setter
public class Country implements Serializable {

    @Serial
    private static final long serialVersionUID = 6425990452740997187L;

    private static final String GENERATOR_NAME = "country_generator";

    @Id
    @SequenceGenerator(
        schema = "public",
        name = GENERATOR_NAME,
        sequenceName = "country_id_seq"
    )
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = GENERATOR_NAME
    )
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 30, unique = true)
    private CountryName name;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Division> divisions;

    @Getter
    public enum CountryName {
        ALEMANIA("ALEMANIA"),
        ARGENTINA("ARGENTINA"),
        BELGICA("BELGICA"),
        BRASIL("BRASIL"),
        BULGARIA("BULGARIA"),
        CHILE("CHILE"),
        COLOMBIA("COLOMBIA"),
        COREA("COREA"),
        COSTARICA("COSTA RICA"),
        DINAMARCA("DINAMARCA"),
        EEUU("EE.UU."),
        ESLOVENIA("ESLOVENIA"),
        ESPAÑA("ESPAÑA"),
        FINLANDIA("FINLANDIA"),
        FRANCIA("FRANCIA"),
        HOLANDA("HOLANDA"),
        INGLATERRA("INGLATERRA"),
        ITALIA("ITALIA"),
        JAPON("JAPON"),
        MEXICO("MEXICO"),
        PARAGUAY("PARAGUAY"),
        PERU("PERU"),
        PORTUGAL("PORTUGAL"),
        REPIRLANDA("REP. IRLANDA"),
        RUMANIA("RUMANIA"),
        RUSIA("RUSIA"),
        SENEGAL("SENEGAL"),
        SUDAFRICA("SUDAFRICA"),
        SUECIA("SUECIA"),
        TURQUIA("TURQUIA"),
        URUGUAY("URUGUAY"),
        YUGOSLAVIA("YUGOSLAVIA");

        private final String value;

        CountryName(final String value) {
            this.value = value;
        }

        public static CountryName fromString(final String value) {
            return Arrays.stream(CountryName.values())
                .filter(countryName -> value.equals(countryName.getValue()))
                .findAny()
                .orElseThrow(() ->
                    new IllegalArgumentException("Unknown country: " + value)
                );
        }
    }

}
