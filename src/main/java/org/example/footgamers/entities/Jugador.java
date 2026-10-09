package org.example.footgamers.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Jugador {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "equipo_favorito")
    private String equipoFavorito;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @OneToMany(cascade = CascadeType.ALL,mappedBy ="jugador")
    private List<ParticipacionPartido> participaciones;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "jugador")
    private List<Trofeo> trofeos;

    @OneToMany(cascade =CascadeType.ALL, mappedBy ="ganador")
    private List<Torneo> torneos;


}