package org.example.footgamers.dto.response;

import java.time.LocalDate;

public record JugadorResponseDto(
        Long id,
        Long usuarioId,
        String username,
        String equipoFavorito,
        LocalDate fechaRegistro
) {
}