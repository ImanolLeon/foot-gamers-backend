package org.example.footgamers.service.reglas;

import org.example.footgamers.dto.request.BandoRequestDto;
import org.example.footgamers.dto.response.BandoResponseDto;
import org.springframework.data.domain.Page;

public interface IBando {

    BandoResponseDto crear(BandoRequestDto request);

    Page<BandoResponseDto> obtenerTodos(Long pagina, Long tamano);

    BandoResponseDto obtenerPorId(Long id);

    BandoResponseDto actualizar(Long id, BandoRequestDto request);

    void eliminar(Long id);
}