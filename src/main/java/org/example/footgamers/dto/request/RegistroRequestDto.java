package org.example.footgamers.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alta publica de una cuenta local desde el formulario de registro.
 *
 * El rol NO viaja desde el cliente: el auto-registro siempre crea un JUGADOR,
 * para evitar que cualquiera se conceda privilegios de administrador.
 */
public record RegistroRequestDto(
        @NotBlank(message = "El username es obligatorio")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]{3,}$",
                message = "El username solo admite letras, números, punto, guion y guion bajo (mínimo 3)")
        String username,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String contrasena
) {
}
