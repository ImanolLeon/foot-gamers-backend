package org.example.footgamers.service.reglas;

import org.example.footgamers.dto.request.ParticipacionPartidoRequestDto;
import org.example.footgamers.dto.response.ParticipacionPartidoResponseDto;
import org.springframework.data.domain.Page;

public interface IParticipacionPartido {

    ParticipacionPartidoResponseDto crear(ParticipacionPartidoRequestDto request);

    Page<ParticipacionPartidoResponseDto> obtenerTodos(Long pagina, Long tamano);

    ParticipacionPartidoResponseDto obtenerPorId(Long id);

    ParticipacionPartidoResponseDto actualizar(Long id, ParticipacionPartidoRequestDto request);

    ParticipacionPartidoResponseDto confirmar(Long id);

    void eliminar(Long id);
}