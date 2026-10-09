package org.example.footgamers.dto.response;

import org.example.footgamers.entities.enums.EstadoTorneo;

import java.time.LocalDate;

public record TorneoInscripcionResponseDto(
        Long id,
        String nombre,
        Long categoriaId,
        String nombreCategoria,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        long cantidadJugadores,
        long inscritos,
        long cuposDisponibles,
        EstadoTorneo estado,
        boolean yaInscrito
) {
}
