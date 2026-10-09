package org.example.footgamers.service.implementacion;

import org.example.footgamers.dto.request.ActualizarPerfilRequestDto;
import org.example.footgamers.dto.request.CambiarContrasenaRequestDto;
import org.example.footgamers.dto.response.LoginResponseDto;
import org.example.footgamers.entities.Jugador;
import org.example.footgamers.entities.Usuario;
import org.example.footgamers.entities.enums.Rol;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.repository.JugadorRepository;
import org.example.footgamers.repository.UsuarioRepository;
import org.example.footgamers.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Edición del propio perfil: username/email (con token renovado) y contraseña
 * (exigiendo la actual en cuentas locales).
 */
class AuthServicePerfilTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final JugadorRepository jugadorRepository = mock(JugadorRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, jugadorRepository, passwordEncoder, jwtService);
        ReflectionTestUtils.setField(authService, "expiracionMs", 3_600_000L);
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void actualizarPerfilGuardaYEmiteTokenNuevo() {
        autenticar(usuario(1L, "viejo", "viejo@test.com", "HASH"));
        Usuario enBd = usuario(1L, "viejo", "viejo@test.com", "HASH");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(enBd));
        when(usuarioRepository.existsByUsername("nuevo")).thenReturn(false);
        when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("jwt-nuevo");

        LoginResponseDto response =
                authService.actualizarPerfil(new ActualizarPerfilRequestDto("nuevo", "nuevo@test.com"));

        assertThat(response.token()).isEqualTo("jwt-nuevo");
        assertThat(response.username()).isEqualTo("nuevo");
        assertThat(enBd.getUsername()).isEqualTo("nuevo");
        assertThat(enBd.getEmail()).isEqualTo("nuevo@test.com");
    }

    @Test
    void actualizarPerfilRechazaUsernameDuplicado() {
        autenticar(usuario(1L, "viejo", "viejo@test.com", "HASH"));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, "viejo", "viejo@test.com", "HASH")));
        when(usuarioRepository.existsByUsername("ocupado")).thenReturn(true);

        assertThatThrownBy(() -> authService.actualizarPerfil(
                new ActualizarPerfilRequestDto("ocupado", "nuevo@test.com")))
                .isInstanceOf(ApiException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarContrasenaExigeLaActualYHasheaLaNueva() {
        autenticar(usuario(1L, "viejo", "viejo@test.com", "HASH"));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, "viejo", "viejo@test.com", "HASH")));
        when(passwordEncoder.matches("actual", "HASH")).thenReturn(true);
        when(passwordEncoder.encode("nueva123")).thenReturn("HASH2");

        authService.cambiarContrasena(new CambiarContrasenaRequestDto("actual", "nueva123"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getContrasena()).isEqualTo("HASH2");
    }

    @Test
    void cambiarContrasenaRechazaActualIncorrecta() {
        autenticar(usuario(1L, "viejo", "viejo@test.com", "HASH"));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, "viejo", "viejo@test.com", "HASH")));
        when(passwordEncoder.matches("mala", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> authService.cambiarContrasena(
                new CambiarContrasenaRequestDto("mala", "nueva123")))
                .isInstanceOf(ApiException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarContrasenaPermiteLaPrimeraEnCuentaOAuth() {
        autenticar(usuario(1L, "oauth", "oauth@test.com", null));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, "oauth", "oauth@test.com", null)));
        when(passwordEncoder.encode("nueva123")).thenReturn("HASH");

        authService.cambiarContrasena(new CambiarContrasenaRequestDto(null, "nueva123"));

        verify(usuarioRepository).save(any(Usuario.class));
    }

    private void autenticar(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    private Usuario usuario(Long id, String username, String email, String contrasena) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setContrasena(contrasena);
        usuario.setRol(Rol.JUGADOR);
        usuario.setProvider("local");
        return usuario;
    }
}
