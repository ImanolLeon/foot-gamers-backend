package org.example.footgamers.dto.response;

import org.example.footgamers.entities.enums.EstadoConfirmacion;

public record ParticipacionPartidoResponseDto(
        Long id,
        Long partidoId,
        Long jugadorId,
        String usernameJugador,
        Long bandoId,
        Integer numeroLado,
        String equipo,
        EstadoConfirmacion estadoConfirmacion
) {
}