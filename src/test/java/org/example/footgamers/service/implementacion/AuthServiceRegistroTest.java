package org.example.footgamers.service.implementacion;

import org.example.footgamers.dto.request.RegistroRequestDto;
import org.example.footgamers.dto.response.LoginResponseDto;
import org.example.footgamers.entities.Jugador;
import org.example.footgamers.entities.Usuario;
import org.example.footgamers.entities.enums.Rol;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.repository.JugadorRepository;
import org.example.footgamers.repository.UsuarioRepository;
import org.example.footgamers.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El auto-registro no recibe rol desde el cliente: este test fija que siempre
 * se crea un JUGADOR, con la contraseña hasheada, su perfil de Jugador y token.
 */
class AuthServiceRegistroTest {

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

    @Test
    void registraSiempreComoJugadorYDevuelveToken() {
        when(usuarioRepository.existsByUsername("nuevo")).thenReturn(false);
        when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("clave123")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("jwt-token");

        LoginResponseDto response = authService.registrar(
                new RegistroRequestDto("nuevo", "nuevo@test.com", "clave123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.username()).isEqualTo("nuevo");
        assertThat(response.rol()).isEqualTo(Rol.JUGADOR);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertThat(guardado.getRol()).isEqualTo(Rol.JUGADOR);
        assertThat(guardado.getProvider()).isEqualTo("local");
        assertThat(guardado.getContrasena()).isEqualTo("HASH");

        verify(jugadorRepository).save(any(Jugador.class));
    }

    @Test
    void rechazaUsernameDuplicado() {
        when(usuarioRepository.existsByUsername("nuevo")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(
                new RegistroRequestDto("nuevo", "nuevo@test.com", "clave123")))
                .isInstanceOf(ApiException.class);

        verify(usuarioRepository, never()).save(any());
        verify(jugadorRepository, never()).save(any());
    }

    @Test
    void rechazaEmailDuplicado() {
        when(usuarioRepository.existsByUsername("nuevo")).thenReturn(false);
        when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(
                new RegistroRequestDto("nuevo", "nuevo@test.com", "clave123")))
                .isInstanceOf(ApiException.class);

        verify(usuarioRepository, never()).save(any());
    }
}
