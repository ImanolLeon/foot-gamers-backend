package org.example.footgamers.repository;

import org.example.footgamers.entities.Torneo;
import org.example.footgamers.entities.enums.EstadoTorneo;
import org.example.footgamers.repository.proyeccion.TorneoParticipantesProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Long> {

    List<Torneo> findByGanador_Id(Long ganador);

    Page<Torneo> findByParticipantes_Id(Long jugadorId, Pageable pageable);

    Page<Torneo> findByEstado(EstadoTorneo estado, Pageable pageable);

    boolean existsByIdAndParticipantes_Id(Long id, Long jugadorId);

    @Query("select t.id as torneoId, count(p) as inscritos "
            + "from Torneo t left join t.participantes p "
            + "where t.id in :torneoIds group by t.id")
    List<TorneoParticipantesProjection> contarParticipantes(@Param("torneoIds") List<Long> torneoIds);

    @Query("select t.id from Torneo t join t.participantes p "
            + "where t.id in :torneoIds and p.id = :jugadorId")
    List<Long> buscarIdsInscritosEn(@Param("torneoIds") List<Long> torneoIds,
                                    @Param("jugadorId") Long jugadorId);
}
