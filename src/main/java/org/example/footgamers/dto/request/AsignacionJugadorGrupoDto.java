package org.example.footgamers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AsignacionJugadorGrupoDto(
        @NotNull(message = "El jugador es obligatorio")
        Long jugadorId,

        @NotBlank(message = "El nombre del grupo es obligatorio")
        String nombreGrupo
) {
}