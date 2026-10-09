package org.example.footgamers.security;

import jakarta.servlet.FilterChain;
import org.example.footgamers.entities.Usuario;
import org.example.footgamers.entities.enums.Rol;
import org.example.footgamers.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Regresion: tras el handshake OAuth2 queda un {@link OAuth2AuthenticationToken}
 * en la sesion. El filtro JWT debe sobrescribirlo cuando llega un Bearer valido,
 * porque la API se autentica por JWT (no por sesion).
 */
class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final JwtAuthenticationFilter filtro =
            new JwtAuthenticationFilter(jwtService, usuarioRepository);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void elTokenJwtGanaAunqueLaSesionTraigaUnUsuarioOAuth() throws Exception {
        // Estado que deja el login OAuth2 en la sesion del navegador.
        OAuth2User principalOAuth = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", "123456", "username", "imanol_google"),
                "sub");
        SecurityContextHolder.getContext().setAuthentication(
                new OAuth2AuthenticationToken(principalOAuth, principalOAuth.getAuthorities(), "google"));

        // La identidad local del mismo usuario, la que viaja en el JWT.
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("imanol_google");
        usuario.setRol(Rol.JUGADOR);
        usuario.setProvider("google");
        usuario.setProviderId("123456");

        when(jwtService.extraerUsername("token-valido")).thenReturn("imanol_google");
        when(usuarioRepository.findByUsername("imanol_google")).thenReturn(Optional.of(usuario));
        when(jwtService.esTokenValido("token-valido", usuario)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/me");
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain cadena = (req, res) -> {
        };

        filtro.doFilter(request, response, cadena);

        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        assertThat(autenticacion).isNotNull();
        assertThat(autenticacion.getPrincipal()).isSameAs(usuario);
        assertThat(autenticacion.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_JUGADOR");
    }

    @Test
    void sinBearerNoTocaLaAutenticacionDeLaSesion() throws Exception {
        OAuth2User principalOAuth = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", "123456"),
                "sub");
        var original = new OAuth2AuthenticationToken(principalOAuth, principalOAuth.getAuthorities(), "google");
        SecurityContextHolder.getContext().setAuthentication(original);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
        });

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(original);
    }
}
