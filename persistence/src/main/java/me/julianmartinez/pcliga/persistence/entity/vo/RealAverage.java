package me.julianmartinez.pcliga.persistence.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RealAverage {

    @Column(name = "squad_average")
    private BigDecimal squadAverage;

    @Column(name = "best_eleven_average")
    private BigDecimal bestElevenAverage;

}
