package org.example.footgamers.service.reglas;

import org.example.footgamers.dto.request.TrofeoRequestDto;
import org.example.footgamers.dto.response.TrofeoResponseDto;
import org.springframework.data.domain.Page;

public interface ITrofeo {

    TrofeoResponseDto crear(TrofeoRequestDto request);

    Page<TrofeoResponseDto> obtenerTodos(Long pagina, Long tamano);

    TrofeoResponseDto obtenerPorId(Long id);

    TrofeoResponseDto actualizar(Long id, TrofeoRequestDto request);

    void eliminar(Long id);
}
