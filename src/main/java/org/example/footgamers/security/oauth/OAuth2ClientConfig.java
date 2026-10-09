package org.example.footgamers.security.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Construye el repositorio de registros OAuth2 a partir de las credenciales de
 * entorno.
 *
 * Se define a mano (en lugar de dejar que lo haga la autoconfiguracion de Spring
 * Boot) porque esta ultima falla al arrancar cuando un {@code client-id} queda
 * vacio. De esta forma el login normal sigue funcionando aunque todavia no haya
 * credenciales de Google o Discord: el proveedor sin configurar se omite y el
 * repositorio queda vacio.
 */
@Configuration
public class OAuth2ClientConfig {

    private static final String REDIRECT_URI = "{baseUrl}/login/oauth2/code/{registrationId}";

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
            @Value("${spring.security.oauth2.client.registration.google.client-secret:}") String googleClientSecret,
            @Value("${spring.security.oauth2.client.registration.discord.client-id:}") String discordClientId,
            @Value("${spring.security.oauth2.client.registration.discord.client-secret:}") String discordClientSecret,
            @Value("${spring.security.oauth2.client.provider.discord.authorization-uri:https://discord.com/oauth2/authorize}") String discordAuthorizationUri,
            @Value("${spring.security.oauth2.client.provider.discord.token-uri:https://discord.com/api/oauth2/token}") String discordTokenUri,
            @Value("${spring.security.oauth2.client.provider.discord.user-info-uri:https://discord.com/api/users/@me}") String discordUserInfoUri,
            @Value("${spring.security.oauth2.client.provider.discord.user-name-attribute:username}") String discordUserNameAttribute) {

        List<ClientRegistration> registrations = new ArrayList<>();
        if (StringUtils.hasText(googleClientId)) {
            registrations.add(google(googleClientId, googleClientSecret));
        }
        if (StringUtils.hasText(discordClientId)) {
            registrations.add(discord(discordClientId, discordClientSecret, discordAuthorizationUri,
                    discordTokenUri, discordUserInfoUri, discordUserNameAttribute));
        }
        return new RepositorioRegistros(registrations);
    }

    private ClientRegistration google(String clientId, String clientSecret) {
        return ClientRegistration.withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(REDIRECT_URI)
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .issuerUri("https://accounts.google.com")
                .clientName("Google")
                .build();
    }

    private ClientRegistration discord(String clientId, String clientSecret,
                                       String authorizationUri, String tokenUri,
                                       String userInfoUri, String userNameAttribute) {
        return ClientRegistration.withRegistrationId("discord")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(REDIRECT_URI)
                .scope("identify", "email")
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .userInfoUri(userInfoUri)
                .userNameAttributeName(userNameAttribute)
                .clientName("Discord")
                .build();
    }

    /**
     * Variante de {@code InMemoryClientRegistrationRepository} que admite cero
     * registros (la de Spring lanza excepcion si la lista esta vacia).
     */
    private static final class RepositorioRegistros
            implements ClientRegistrationRepository, Iterable<ClientRegistration> {

        private final Map<String, ClientRegistration> registros;

        private RepositorioRegistros(List<ClientRegistration> registrations) {
            Map<String, ClientRegistration> mapa = new LinkedHashMap<>();
            for (ClientRegistration registration : registrations) {
                mapa.put(registration.getRegistrationId(), registration);
            }
            this.registros = Collections.unmodifiableMap(mapa);
        }

        @Override
        public ClientRegistration findByRegistrationId(String registrationId) {
            Assert.hasText(registrationId, "registrationId cannot be empty");
            return this.registros.get(registrationId);
        }

        @Override
        public Iterator<ClientRegistration> iterator() {
            return this.registros.values().iterator();
        }
    }
}
