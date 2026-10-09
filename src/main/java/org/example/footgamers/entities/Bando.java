package org.example.footgamers.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bando {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    @Column(name = "numero_lado")
    private int numeroLado;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "bando")
    private List<ParticipacionPartido> participacionPartidos;

}
