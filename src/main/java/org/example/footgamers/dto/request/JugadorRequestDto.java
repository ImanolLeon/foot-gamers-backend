package org.example.footgamers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JugadorRequestDto(
        @NotNull(message = "El usuario es obligatorio")
        Long usuarioId,

        @NotBlank(message = "El equipo favorito es obligatorio")
        String equipoFavorito
) {
}