package org.example.footgamers.repository;

import org.example.footgamers.entities.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    Optional<Jugador> findByUsuario_Username(String username);

    Optional<Jugador> findByUsuario_Id(Long usuarioId);
}