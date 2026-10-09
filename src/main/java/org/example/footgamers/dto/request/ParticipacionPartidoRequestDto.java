package org.example.footgamers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ParticipacionPartidoRequestDto(
        @NotNull(message = "El partido es obligatorio")
        Long partidoId,

        @NotNull(message = "El jugador es obligatorio")
        Long jugadorId,

        @NotNull(message = "El bando es obligatorio")
        Long bandoId,

        @NotBlank(message = "El equipo es obligatorio")
        String equipo
) {
}