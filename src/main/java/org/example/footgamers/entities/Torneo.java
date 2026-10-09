package org.example.footgamers.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.footgamers.entities.enums.EstadoTorneo;
import org.example.footgamers.entities.enums.FaseTorneo;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Torneo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @ManyToOne()
    @JoinColumn(name = "ganador_id")
    private Jugador ganador;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "torneo")
    private List<Partido> partidos;

    private long cantidadJugadores;
    @ManyToOne
    @JoinColumn(name = "id_trofeo")
    private Trofeo trofeo;

    private long cantidadGrupos;

    @Enumerated(EnumType.STRING)
    @Column(name = "fase_actual")
    private FaseTorneo faseActual;

    @Enumerated(EnumType.STRING)
    private EstadoTorneo estado;

    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL)
    private List<Grupo> grupos;

    @ManyToMany
    @JoinTable(name = "torneo_jugador",
            joinColumns = @JoinColumn(name = "torneo_id"),
            inverseJoinColumns = @JoinColumn(name = "jugador_id"))
    private List<Jugador> participantes;

}
