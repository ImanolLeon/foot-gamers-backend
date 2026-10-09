package org.example.footgamers.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.footgamers.entities.enums.TipoAsignacionGrupos;

import java.util.List;

public record AsignacionesGruposRequestDto(
        @NotNull(message = "El tipo de asignación es obligatorio")
        TipoAsignacionGrupos tipoAsignacion,

        List<AsignacionJugadorGrupoDto> asignaciones
) {
}