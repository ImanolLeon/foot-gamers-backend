package org.example.footgamers.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.footgamers.entities.Usuario;
import org.example.footgamers.security.JwtService;
import org.example.footgamers.service.implementacion.OAuthUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Tras un login OAuth correcto: obtiene/crea el usuario, emite el mismo JWT que
 * usa el login normal y redirige al frontend con el token en el query string.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthUserService oauthUserService;
    private final JwtService jwtService;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(OAuthUserService oauthUserService,
                                     JwtService jwtService,
                                     @Value("${app.oauth.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.oauthUserService = oauthUserService;
        this.jwtService = jwtService;
        this.frontendUrl = quitarBarraFinal(frontendUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

        Usuario usuario = oauthUserService.findOrCreateFromOAuth(provider, attributes);
        String token = jwtService.generarToken(usuario);

        // La API es stateless (JWT): cerramos la sesion temporal del handshake
        // para que no quede una autenticacion OAuth2 en el servidor. Si no, esa
        // sesion "tapa" al Bearer token en las siguientes peticiones.
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        String destino = frontendUrl + "/auth/callback?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
        response.sendRedirect(destino);
    }

    private static String quitarBarraFinal(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
