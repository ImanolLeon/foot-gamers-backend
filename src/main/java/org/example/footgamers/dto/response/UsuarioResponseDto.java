package org.example.footgamers.dto.response;

import org.example.footgamers.entities.enums.Rol;

public record UsuarioResponseDto(
        Long id,
        String username,
        String email,
        Rol rol,
        String imagen,
        String avatarUrl,
        String provider
) {
}
