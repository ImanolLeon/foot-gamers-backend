package org.example.footgamers.dto.response;

import java.util.List;

public record HistorialJugadorResponseDto(
        Long jugadorId,
        String username,
        String equipoFavorito,
        Long partidosJugados,
        Long victorias,
        Long empates,
        Long derrotas,
        Long golesAFavor,
        Long golesEnContra,
        Long torneosGanados,
        List<HistorialPartidoResponseDto> partidos
) {
}