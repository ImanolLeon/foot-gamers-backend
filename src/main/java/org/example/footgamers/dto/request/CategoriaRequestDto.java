package org.example.footgamers.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El formato es obligatorio")
        String formato
) {
}