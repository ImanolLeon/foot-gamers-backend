package org.example.footgamers.service.reglas;

import org.example.footgamers.dto.request.UsuarioRequestDto;
import org.example.footgamers.dto.response.UsuarioResponseDto;
import org.example.footgamers.service.almacen.AlmacenImagenes;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface IUsuario {

    UsuarioResponseDto crear(UsuarioRequestDto request);

    Page<UsuarioResponseDto> obtenerTodos(Long pagina, Long tamano);

    UsuarioResponseDto obtenerPorId(Long id);

    UsuarioResponseDto actualizar(Long id, UsuarioRequestDto request);

    void eliminar(Long id);

    UsuarioResponseDto actualizarImagen(Long id, MultipartFile archivo);

    AlmacenImagenes.ImagenGuardada obtenerImagen(Long id);
}
