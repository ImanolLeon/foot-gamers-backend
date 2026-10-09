package org.example.footgamers.service.reglas;

import org.example.footgamers.dto.request.ActualizarPerfilRequestDto;
import org.example.footgamers.dto.request.CambiarContrasenaRequestDto;
import org.example.footgamers.dto.request.LoginRequestDto;
import org.example.footgamers.dto.request.RegistroRequestDto;
import org.example.footgamers.dto.response.LoginResponseDto;
import org.example.footgamers.entities.Jugador;
import org.example.footgamers.entities.Usuario;

public interface IAuth {

    LoginResponseDto login(LoginRequestDto request);

    /** Alta publica de una cuenta local: siempre crea un JUGADOR y devuelve el token. */
    LoginResponseDto registrar(RegistroRequestDto request);

    /** Edita username/email del propio usuario y devuelve un token nuevo. */
    LoginResponseDto actualizarPerfil(ActualizarPerfilRequestDto request);

    /** Cambia la contraseña del propio usuario (exige la actual si ya tiene). */
    void cambiarContrasena(CambiarContrasenaRequestDto request);

    Usuario usuarioActual();

    Jugador jugadorActual();
}