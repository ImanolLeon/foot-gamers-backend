package org.example.footgamers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TrofeoRequestDto(
        @NotBlank(message = "El nombre del trofeo es obligatorio")
        @Size(max = 120, message = "El nombre del trofeo no puede superar los 120 caracteres")
        String nombre,

        LocalDate fecha,

        Long jugadorId
) {
}
