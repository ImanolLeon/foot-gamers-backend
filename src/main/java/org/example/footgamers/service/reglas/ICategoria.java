package org.example.footgamers.service.reglas;

import org.example.footgamers.dto.request.CategoriaRequestDto;
import org.example.footgamers.dto.response.CategoriaResponseDto;
import org.springframework.data.domain.Page;

public interface ICategoria {

    CategoriaResponseDto crear(CategoriaRequestDto request);

    Page<CategoriaResponseDto> obtenerTodos(Long pagina, Long tamano);

    CategoriaResponseDto obtenerPorId(Long id);

    CategoriaResponseDto actualizar(Long id, CategoriaRequestDto request);

    void eliminar(Long id);
}