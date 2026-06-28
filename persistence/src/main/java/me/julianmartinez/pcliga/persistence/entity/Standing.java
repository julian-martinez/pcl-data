package me.julianmartinez.pcliga.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "standing")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Setter
public class Standing implements Serializable {

    @Serial
    private static final long serialVersionUID = 8490649377423151246L;

    @Id
    private Long id;

    @Column(name = "position")
    private Integer position;

    @Column(name = "points")
    private Integer points;

    @Column(name = "played", length = 2)
    private String played;

    @Column(name = "won", length = 2)
    private String won;

    @Column(name = "draw", length = 2)
    private String draw;

    @Column(name = "lost", length = 2)
    private String lost;

    @Column(name = "goals_scored", length = 3)
    private String gScored;

    @Column(name = "goals_conceded", length = 3)
    private String gConceded;

    @Column(name = "goal_average", length = 4)
    private String gAverage;
    
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Club club;
}
