package org.example.footgamers.dto.response;

public record ClasificacionResponseDto(
        Long grupoId,
        String nombreGrupo,
        Long jugadorId,
        String username,
        Integer partidosJugados,
        Integer ganados,
        Integer empatados,
        Integer perdidos,
        Integer golesAFavor,
        Integer golesEnContra,
        Integer puntos
) {
}