package org.example.footgamers.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.footgamers.entities.enums.TipoPartido;

import java.time.LocalDate;

public record PartidoRequestDto(
        Long torneoId,

        @NotNull(message = "El tipo de partido es obligatorio")
        TipoPartido tipoPartido,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha
) {
}