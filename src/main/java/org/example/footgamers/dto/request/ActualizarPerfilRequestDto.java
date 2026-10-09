package org.example.footgamers.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Datos editables del propio perfil desde el panel.
 *
 * El rol, el proveedor y la imagen NO se tocan aquí: el rol no puede cambiar
 * (evita escalada de privilegios) y la imagen tiene su propio endpoint.
 */
public record ActualizarPerfilRequestDto(
        @NotBlank(message = "El username es obligatorio")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]{3,}$",
                message = "El username solo admite letras, números, punto, guion y guion bajo (mínimo 3)")
        String username,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es válido")
        String email
) {
}
