package org.example.footgamers.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.footgamers.entities.enums.EstadoTorneo;

public record CambiarEstadoTorneoRequestDto(
        @NotNull(message = "El estado es obligatorio")
        EstadoTorneo estado
) {
}
