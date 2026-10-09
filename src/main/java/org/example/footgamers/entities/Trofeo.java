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
public class Trofeo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ManyToOne()
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;


    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "trofeo")
    private List<Torneo> torneos;

    private LocalDate fecha;


}
