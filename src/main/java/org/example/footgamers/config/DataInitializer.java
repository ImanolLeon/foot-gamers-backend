package org.example.footgamers.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.footgamers.entities.Jugador;
import org.example.footgamers.entities.Usuario;
import org.example.footgamers.entities.enums.Rol;
import org.example.footgamers.repository.JugadorRepository;
import org.example.footgamers.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String USERNAME_ADMIN = "imanol";

    private final UsuarioRepository usuarioRepository;
    private final JugadorRepository jugadorRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * El usuario y el perfil de jugador se crean por separado a proposito: si el
     * usuario ya existia pero le faltaba el Jugador, igual se completa.
     */
    @Override
    @Transactional
    public void run(String... args) {
        Usuario admin = usuarioRepository.findByUsername(USERNAME_ADMIN).orElseGet(this::crearAdmin);
        if (jugadorRepository.findByUsuario_Id(admin.getId()).isPresent()) {
            return;
        }
        Jugador jugador = jugadorRepository.save(crearJugador(admin));
        log.info("Perfil de jugador creado para {}: usuarioId={}, jugadorId={}",
                USERNAME_ADMIN, admin.getId(), jugador.getId());
    }

    private Usuario crearAdmin() {
        Usuario admin = new Usuario();
        admin.setUsername(USERNAME_ADMIN);
        admin.setEmail("imanol@footgamers.com");
        admin.setContrasena(passwordEncoder.encode("123456"));
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setProvider("local");
        return usuarioRepository.save(admin);
    }

    private Jugador crearJugador(Usuario usuario) {
        Jugador jugador = new Jugador();
        jugador.setUsuario(usuario);
        jugador.setEquipoFavorito("San Lorenzo");
        jugador.setFechaRegistro(LocalDate.now());
        jugador.setParticipaciones(new ArrayList<>());
        jugador.setTrofeos(new ArrayList<>());
        jugador.setTorneos(new ArrayList<>());
        return jugador;
    }
}
