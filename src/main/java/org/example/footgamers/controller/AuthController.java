package org.example.footgamers.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.ActualizarPerfilRequestDto;
import org.example.footgamers.dto.request.CambiarContrasenaRequestDto;
import org.example.footgamers.dto.request.LoginRequestDto;
import org.example.footgamers.dto.request.RegistroRequestDto;
import org.example.footgamers.dto.response.LoginResponseDto;
import org.example.footgamers.dto.response.UsuarioResponseDto;
import org.example.footgamers.entities.Usuario;
import org.example.footgamers.service.reglas.IAuth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuth authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** Auto-registro publico: crea la cuenta como JUGADOR e inicia sesion. */
    @PostMapping("/register")
    public ResponseEntity<LoginResponseDto> registrar(@Valid @RequestBody RegistroRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDto> me() {
        Usuario usuario = authService.usuarioActual();
        return ResponseEntity.ok(new UsuarioResponseDto(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getImagen(),
                usuario.getAvatarUrl(),
                usuario.getProvider()
        ));
    }

    /** Edita username/email del propio usuario y devuelve un token renovado. */
    @PutMapping("/me")
    public ResponseEntity<LoginResponseDto> actualizarPerfil(
            @Valid @RequestBody ActualizarPerfilRequestDto request) {
        return ResponseEntity.ok(authService.actualizarPerfil(request));
    }

    /** Cambia la contraseña del propio usuario. */
    @PutMapping("/me/contrasena")
    public ResponseEntity<Void> cambiarContrasena(
            @Valid @RequestBody CambiarContrasenaRequestDto request) {
        authService.cambiarContrasena(request);
        return ResponseEntity.noContent().build();
    }
}