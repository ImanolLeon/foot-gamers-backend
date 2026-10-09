package org.example.footgamers.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Clasificacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;

    @Column(name = "partidos_jugados")
    private int partidosJugados;

    private int ganados;

    private int empatados;

    private int perdidos;

    @Column(name = "goles_a_favor")
    private int golesAFavor;

    @Column(name = "goles_en_contra")
    private int golesEnContra;

    private int puntos;
}