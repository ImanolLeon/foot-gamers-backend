package org.example.footgamers.service.reglas;

import org.example.footgamers.dto.request.CambiarEstadoTorneoRequestDto;
import org.example.footgamers.dto.request.TorneoRequestDto;
import org.example.footgamers.dto.response.TorneoInscripcionResponseDto;
import org.example.footgamers.dto.response.TorneoResponseDto;
import org.springframework.data.domain.Page;

public interface ITorneo {

    TorneoResponseDto crear(TorneoRequestDto request);

    Page<TorneoResponseDto> obtenerTodos(Long pagina, Long tamano);

    Page<TorneoInscripcionResponseDto> obtenerDisponibles(Long pagina, Long tamano);

    TorneoResponseDto obtenerPorId(Long id);

    TorneoResponseDto actualizar(Long id, TorneoRequestDto request);

    TorneoResponseDto asignarGanador(Long id, Long jugadorId);

    TorneoResponseDto cambiarEstado(Long id, CambiarEstadoTorneoRequestDto request);

    TorneoResponseDto inscribirJugador(Long id, Long jugadorId);

    TorneoResponseDto inscribirme(Long id);

    void eliminar(Long id);
}