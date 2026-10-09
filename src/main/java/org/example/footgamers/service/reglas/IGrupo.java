package org.example.footgamers.service.reglas;

import org.example.footgamers.dto.request.AsignacionesGruposRequestDto;
import org.example.footgamers.dto.response.ClasificacionResponseDto;
import org.example.footgamers.dto.response.GrupoResponseDto;
import org.example.footgamers.dto.response.PartidoResponseDto;

import java.util.List;

public interface IGrupo {

    List<GrupoResponseDto> crearGrupos(Long torneoId, AsignacionesGruposRequestDto request);

    List<GrupoResponseDto> listarGrupos(Long torneoId);

    List<ClasificacionResponseDto> clasificacion(Long grupoId);

    List<PartidoResponseDto> generarPartidosGrupos(Long torneoId);

    List<PartidoResponseDto> generarLlaves(Long torneoId);

    List<PartidoResponseDto> proximaRonda(Long torneoId);
}