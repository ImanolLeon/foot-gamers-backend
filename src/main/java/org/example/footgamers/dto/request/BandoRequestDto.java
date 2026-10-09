package org.example.footgamers.dto.request;

import jakarta.validation.constraints.NotNull;

public record BandoRequestDto(
        @NotNull(message = "El número de lado es obligatorio")
        Integer numeroLado
) {
}