package org.example.footgamers.dto.response;

import org.example.footgamers.entities.enums.EstadoConfirmacion;
import org.example.footgamers.entities.enums.FaseTorneo;
import org.example.footgamers.entities.enums.TipoPartido;

import java.time.LocalDate;

public record HistorialPartidoResponseDto(
        Long partidoId,
        Long torneoId,
        String nombreTorneo,
        TipoPartido tipoPartido,
        FaseTorneo fase,
        LocalDate fecha,
        String rival,
        String resultado,
        String resultadoPropio,
        String resultadoRival,
        String desenlace,
        String estadoConfirmacion
) {
}