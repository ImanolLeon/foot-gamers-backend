package org.example.footgamers.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.footgamers.entities.enums.EstadoConfirmacion;

@Entity(name = "participacion_partido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipacionPartido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    private Partido partido;

    private String equipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_confirmacion")
    private EstadoConfirmacion estadoConfirmacion;

    @ManyToOne()
    @JoinColumn(name = "bando_id")
    private Bando bando;

    @ManyToOne()
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;

    @ManyToOne()
    @JoinColumn(name = "bando_ganador_votado_id")
    private Bando bandoGanadorVotado;
}
