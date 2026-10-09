package org.example.footgamers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cambio de contraseña desde el perfil.
 *
 * {@code actual} es obligatoria en cuentas locales. En una cuenta OAuth que
 * todavía no tiene contraseña puede ir vacía: se establece la primera.
 */
public record CambiarContrasenaRequestDto(
        String actual,

        @NotBlank(message = "La contraseña nueva es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String nueva
) {
}
