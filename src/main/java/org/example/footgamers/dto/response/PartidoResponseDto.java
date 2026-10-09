package org.example.footgamers.dto.response;

import org.example.footgamers.entities.enums.EstadoConfirmacion;
import org.example.footgamers.entities.enums.EstadoPartido;
import org.example.footgamers.entities.enums.FaseTorneo;
import org.example.footgamers.entities.enums.TipoPartido;

import java.time.LocalDate;

public record PartidoResponseDto(
        Long id,
        String resultado,
        Long ganadorId,
        Long torneoId,
        TipoPartido tipoPartido,
        EstadoConfirmacion estadoConfirmacion,
        LocalDate fecha,
        FaseTorneo fase,
        Long grupoId,
        EstadoPartido estado
) {
}