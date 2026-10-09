package org.example.footgamers.service.implementacion;

import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.CategoriaRequestDto;
import org.example.footgamers.dto.response.CategoriaResponseDto;
import org.example.footgamers.entities.Categoria;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.exception.MensajeException;
import org.example.footgamers.repository.CategoriaRepository;
import org.example.footgamers.service.reglas.ICategoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService implements ICategoria {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public CategoriaResponseDto crear(CategoriaRequestDto request) {
        Categoria categoria = new Categoria();
        categoria.setNombre(request.nombre());
        categoria.setFormato(request.formato());
        return toResponse(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponseDto> obtenerTodos(Long pagina, Long tamano) {
        return categoriaRepository.findAll(PageRequest.of(pagina.intValue(), tamano.intValue()))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDto obtenerPorId(Long id) {
        return toResponse(buscarCategoria(id));
    }

    @Override
    @Transactional
    public CategoriaResponseDto actualizar(Long id, CategoriaRequestDto request) {
        Categoria categoria = buscarCategoria(id);
        categoria.setNombre(request.nombre());
        categoria.setFormato(request.formato());
        return toResponse(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        categoriaRepository.delete(buscarCategoria(id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "categoria", id)));
    }

    private CategoriaResponseDto toResponse(Categoria categoria) {
        return new CategoriaResponseDto(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getFormato()
        );
    }
}