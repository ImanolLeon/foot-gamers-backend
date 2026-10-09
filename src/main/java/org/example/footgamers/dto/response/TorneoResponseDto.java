package org.example.footgamers.dto.response;

import org.example.footgamers.entities.enums.EstadoTorneo;
import org.example.footgamers.entities.enums.FaseTorneo;

import java.time.LocalDate;

public record TorneoResponseDto(
        Long id,
        String nombre,
        Long categoriaId,
        String nombreCategoria,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Long ganadorId,
        String usernameGanador,
        Long cantidadJugadores,
        Long cantidadGrupos,
        FaseTorneo faseActual,
        EstadoTorneo estado
) {
}