package org.example.footgamers.dto.response;

import org.example.footgamers.entities.enums.Rol;

public record LoginResponseDto(
        String token,
        String username,
        Rol rol,
        Long expiraEnMs
) {
}