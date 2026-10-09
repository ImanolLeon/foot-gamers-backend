package org.example.footgamers.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.footgamers.entities.enums.EstadoConfirmacion;
import org.example.footgamers.entities.enums.EstadoPartido;
import org.example.footgamers.entities.enums.FaseTorneo;
import org.example.footgamers.entities.enums.TipoPartido;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Partido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resultado;

    @OneToOne
    @JoinColumn(name = "bando_id")
    private Bando ganador;

    @ManyToOne()
    @JoinColumn(name = "torneo_id")
    private Torneo torneo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_partido")
    private TipoPartido tipoPartido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_confirmacion")
    private EstadoConfirmacion estadoConfirmacion;

    private LocalDate fecha;

    @OneToMany(mappedBy = "partido",cascade = CascadeType.ALL)
    private List<ParticipacionPartido> participacionPartidos;

    @Enumerated(EnumType.STRING)
    private FaseTorneo fase;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @Enumerated(EnumType.STRING)
    private EstadoPartido estado;

    @ManyToOne
    @JoinColumn(name = "partido_fuente_uno_id")
    private Partido fuenteUno;

    @ManyToOne
    @JoinColumn(name = "partido_fuente_dos_id")
    private Partido fuenteDos;
}
