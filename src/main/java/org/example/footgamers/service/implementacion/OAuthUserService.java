package org.example.footgamers.service.implementacion;

import lombok.RequiredArgsConstructor;
import org.example.footgamers.entities.Jugador;
import org.example.footgamers.entities.Usuario;
import org.example.footgamers.entities.enums.Rol;
import org.example.footgamers.exception.ApiException;
import org.example.footgamers.repository.JugadorRepository;
import org.example.footgamers.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Alta de usuarios desde un proveedor OAuth2.
 *
 * La identidad se resuelve por {@code (provider, providerId)}, nunca por email:
 * vincular por email permitiria que un atacante reclamara cuentas ajenas.
 */
@Service
@RequiredArgsConstructor
public class OAuthUserService {

    private static final String PROVEEDOR_GOOGLE = "google";
    private static final String PROVEEDOR_DISCORD = "discord";

    private final UsuarioRepository usuarioRepository;
    private final JugadorRepository jugadorRepository;

    @Transactional
    public Usuario findOrCreateFromOAuth(String provider, Map<String, Object> attributes) {
        PerfilOAuth perfil = extraerPerfil(provider, attributes);
        return usuarioRepository.findByProviderAndProviderId(provider, perfil.providerId())
                .orElseGet(() -> crearUsuario(provider, perfil));
    }

    private PerfilOAuth extraerPerfil(String provider, Map<String, Object> attributes) {
        if (PROVEEDOR_GOOGLE.equals(provider)) {
            String providerId = texto(attributes.get("sub"));
            if (!StringUtils.hasText(providerId)) {
                throw ApiException.noAutorizado("Google no devolvió el identificador de usuario.");
            }
            boolean emailVerificado = Boolean.TRUE.equals(attributes.get("email_verified"));
            String email = emailVerificado ? texto(attributes.get("email")) : null;
            String base = StringUtils.hasText(texto(attributes.get("name")))
                    ? texto(attributes.get("name"))
                    : email;
            return new PerfilOAuth(providerId, email, texto(attributes.get("picture")), base);
        }

        if (PROVEEDOR_DISCORD.equals(provider)) {
            String providerId = texto(attributes.get("id"));
            if (!StringUtils.hasText(providerId)) {
                throw ApiException.noAutorizado("Discord no devolvió el identificador de usuario.");
            }
            boolean emailVerificado = Boolean.TRUE.equals(attributes.get("verified"));
            String email = emailVerificado ? texto(attributes.get("email")) : null;
            String avatarHash = texto(attributes.get("avatar"));
            // Discord no siempre trae avatar: en ese caso se deja null.
            String avatar = StringUtils.hasText(avatarHash)
                    ? "https://cdn.discordapp.com/avatars/" + providerId + "/" + avatarHash + ".png"
                    : null;
            return new PerfilOAuth(providerId, email, avatar, texto(attributes.get("username")));
        }

        throw ApiException.solicitudInvalida("Proveedor OAuth no soportado: " + provider);
    }

    private Usuario crearUsuario(String provider, PerfilOAuth perfil) {
        Usuario usuario = new Usuario();
        usuario.setProvider(provider);
        usuario.setProviderId(perfil.providerId());
        usuario.setEmail(perfil.email());
        usuario.setAvatarUrl(perfil.avatarUrl());
        usuario.setContrasena(null);
        usuario.setRol(Rol.JUGADOR);
        usuario.setUsername(generarUsernameUnico(perfil.usernameBase()));

        Usuario guardado = usuarioRepository.save(usuario);
        // Sin perfil de Jugador, los endpoints /jugadores/mis-* devolverian 404.
        jugadorRepository.save(crearJugador(guardado));
        return guardado;
    }

    private String generarUsernameUnico(String base) {
        String limpio = base == null ? "" : base.toLowerCase().replaceAll("[^a-z0-9._-]", "");
        if (limpio.length() < 3) {
            limpio = "jugador";
        }
        if (!usuarioRepository.existsByUsername(limpio)) {
            return limpio;
        }
        // Si el username ya existe, se agrega un sufijo numerico aleatorio.
        for (int intento = 0; intento < 20; intento++) {
            String candidato = limpio + ThreadLocalRandom.current().nextInt(1000, 10000);
            if (!usuarioRepository.existsByUsername(candidato)) {
                return candidato;
            }
        }
        return limpio + System.currentTimeMillis();
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

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private record PerfilOAuth(String providerId, String email, String avatarUrl, String usernameBase) {
    }
}
