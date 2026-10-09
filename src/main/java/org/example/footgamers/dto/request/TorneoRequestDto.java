package org.example.footgamers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TorneoRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        @NotNull(message = "La cantidad de jugadores es obligatoria")
        Long cantidadJugadores,

        @NotNull(message = "El trofeo es obligatorio")
        Long trofeoId,

        @NotNull(message = "La cantidad de grupos es obligatoria")
        Long cantidadGrupos
) {
}