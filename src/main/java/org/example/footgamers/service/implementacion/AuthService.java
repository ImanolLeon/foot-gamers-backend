package org.example.footgamers.service.implementacion;

import lombok.RequiredArgsConstructor;
import org.example.footgamers.dto.request.ActualizarPerfilRequestDto;
import org.example.footgamers.dto.request.CambiarContrasenaRequestDto;
import org.example.footgamers.dto.request.LoginRequestDto;
import org.example.footgamers.dto.request.RegistroRequestDto;
import org.example.footgamers.dto.response.LoginResponseDto;
import org.example.footgamers.entities.Jugador;
import org.example.footgamers.entities.Usuario;
import org.example.footgamers.entities.enums.Rol;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.exception.MensajeException;
import org.example.footgamers.repository.JugadorRepository;
import org.example.footgamers.repository.UsuarioRepository;
import org.example.footgamers.security.JwtService;
import org.example.footgamers.service.reglas.IAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuth {

    private final UsuarioRepository usuarioRepository;
    private final JugadorRepository jugadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.expiration-ms}")
    private long expiracionMs;

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> ApiException.noAutorizado("Credenciales inválidas"));
        // Las cuentas OAuth no tienen contrasena: error controlado en vez de
        // dejar que passwordEncoder reciba null.
        if (usuario.getContrasena() == null) {
            throw ApiException.noAutorizado(
                    "Esta cuenta inicia sesión con " + usuario.getProvider() + ".");
        }
        if (!passwordEncoder.matches(request.contrasena(), usuario.getContrasena())) {
            throw ApiException.noAutorizado("Credenciales inválidas");
        }
        return new LoginResponseDto(
                jwtService.generarToken(usuario),
                usuario.getUsername(),
                usuario.getRol(),
                expiracionMs
        );
    }

    @Override
    @Transactional
    public LoginResponseDto registrar(RegistroRequestDto request) {
        String username = request.username().trim();
        String email = request.email().trim();

        if (usuarioRepository.existsByUsername(username)) {
            throw ApiException.valorYaEnUso(
                    String.format(MensajeException.VALOR_YA_EN_USO, "usuario", "username", username));
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw ApiException.valorYaEnUso(
                    String.format(MensajeException.VALOR_YA_EN_USO, "usuario", "email", email));
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        // El rol no viene del cliente: el auto-registro siempre es JUGADOR.
        usuario.setRol(Rol.JUGADOR);
        usuario.setProvider("local");
        Usuario guardado = usuarioRepository.save(usuario);

        // Perfil de jugador para que /jugadores/mis-* funcione desde el primer dia.
        jugadorRepository.save(crearJugador(guardado));

        return new LoginResponseDto(
                jwtService.generarToken(guardado),
                guardado.getUsername(),
                guardado.getRol(),
                expiracionMs
        );
    }

    private Jugador crearJugador(Usuario usuario) {
        Jugador jugador = new Jugador();
        jugador.setUsuario(usuario);
        jugador.setFechaRegistro(LocalDate.now());
        jugador.setParticipaciones(new ArrayList<>());
        jugador.setTrofeos(new ArrayList<>());
        jugador.setTorneos(new ArrayList<>());
        return jugador;
    }

    @Override
    @Transactional
    public LoginResponseDto actualizarPerfil(ActualizarPerfilRequestDto request) {
        Usuario usuario = buscarPorId(usuarioActual().getId());
        String username = request.username().trim();
        String email = request.email().trim();

        if (!usuario.getUsername().equals(username) && usuarioRepository.existsByUsername(username)) {
            throw ApiException.valorYaEnUso(
                    String.format(MensajeException.VALOR_YA_EN_USO, "usuario", "username", username));
        }
        if (!usuario.getEmail().equals(email) && usuarioRepository.existsByEmail(email)) {
            throw ApiException.valorYaEnUso(
                    String.format(MensajeException.VALOR_YA_EN_USO, "usuario", "email", email));
        }

        usuario.setUsername(username);
        usuario.setEmail(email);
        Usuario guardado = usuarioRepository.save(usuario);

        // El username viaja en el `sub` del JWT: emitimos un token nuevo para
        // que cambiarlo no invalide la sesión en curso.
        return new LoginResponseDto(
                jwtService.generarToken(guardado),
                guardado.getUsername(),
                guardado.getRol(),
                expiracionMs
        );
    }

    @Override
    @Transactional
    public void cambiarContrasena(CambiarContrasenaRequestDto request) {
        Usuario usuario = buscarPorId(usuarioActual().getId());
        // Cuentas locales: se exige la contraseña actual. Una cuenta OAuth sin
        // contraseña todavía puede establecer la primera.
        if (usuario.getContrasena() != null
                && (!StringUtils.hasText(request.actual())
                    || !passwordEncoder.matches(request.actual(), usuario.getContrasena()))) {
            throw ApiException.noAutorizado("La contraseña actual no es correcta");
        }
        usuario.setContrasena(passwordEncoder.encode(request.nueva()));
        usuarioRepository.save(usuario);
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "usuario", id)));
    }

    @Override
    public Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario usuario)) {
            throw ApiException.noAutorizado("Debe iniciar sesión");
        }
        return usuario;
    }

    @Override
    @Transactional(readOnly = true)
    public Jugador jugadorActual() {
        Usuario usuario = usuarioActual();
        return jugadorRepository.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> ApiException.noEncontrado(
                        String.format(MensajeException.VALOR_NO_ENCONTRADO, "jugador", usuario.getUsername())));
    }
}