package org.example.footgamers.dto.response;

import java.time.LocalDate;

public record TrofeoResponseDto(
        Long id,
        String nombre,
        Long jugadorId,
        String username,
        LocalDate fecha
) {
}
