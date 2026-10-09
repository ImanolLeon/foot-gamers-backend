package org.example.footgamers.repository;

import org.example.footgamers.entities.Clasificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClasificacionRepository extends JpaRepository<Clasificacion, Long> {

    List<Clasificacion> findByGrupo_Id(Long grupoId);

    Optional<Clasificacion> findByGrupo_IdAndJugador_Id(Long grupoId, Long jugadorId);
}