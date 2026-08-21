package es.caib.invai.back.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SecurityConfig}, focused exclusively on the {@code oidcUserService(...)}
 * bean method - the one piece of this configuration class with real conditional logic (mapping
 * the {@code resource_access.<client-id>.roles} JWT claim into Spring Security authorities, including the
 * null-safe fallback to an empty role list). The {@code filterChain(...)} bean requires a full
 * Spring Security filter chain context to exercise meaningfully and is intentionally not covered
 * here; {@code jwtDecoder()} and {@code httpFirewall()} are one-line delegations with nothing to
 * branch on.
 * <p>
 * {@code oidcUserService(...)} builds the {@link OidcUser} directly from the ID token's own claims
 * and never performs any outbound HTTP call (no UserInfo endpoint request), so no HTTP client needs
 * mocking here.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    private static final String CLIENT_ID = "soffid-client";

    @Mock
    private JwtDecoder jwtDecoder;

    private final SecurityConfig securityConfig = new SecurityConfig();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(securityConfig, "clientId", CLIENT_ID);
    }

    private ClientRegistration clientRegistration() {
        return ClientRegistration.withRegistrationId("soffid")
                .clientId("client-id")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://idp.example.com/auth")
                .tokenUri("https://idp.example.com/token")
                .clientName("Soffid")
                .build();
    }

    private OidcUserRequest userRequest() {
        OidcIdToken idToken = new OidcIdToken("id-token-value", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("sub", "user-1"));
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                "access-token-value", Instant.now(), Instant.now().plusSeconds(300));
        return new OidcUserRequest(clientRegistration(), accessToken, idToken);
    }

    @Test
    void oidcUserService_resourceAccessWithRoles_mapsToRolePrefixedAuthorities() {
        Jwt jwt = Jwt.withTokenValue("access-token-value")
                .header("alg", "none")
                .claim("resource_access", Map.of(CLIENT_ID, Map.of("roles", List.of("admin", "usuari-tipus-E"))))
                .build();
        when(jwtDecoder.decode("access-token-value")).thenReturn(jwt);

        OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = securityConfig.oidcUserService(jwtDecoder);
        OidcUser oidcUser = oidcUserService.loadUser(userRequest());

        Set<String> authorityNames = oidcUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(Set.of("ROLE_admin", "ROLE_usuari-tipus-E"), authorityNames);
    }

    @Test
    void oidcUserService_noResourceAccessClaim_resultsInNoAuthorities() {
        Jwt jwt = Jwt.withTokenValue("access-token-value")
                .header("alg", "none")
                .claim("sub", "user-1")
                .build();
        when(jwtDecoder.decode("access-token-value")).thenReturn(jwt);

        OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = securityConfig.oidcUserService(jwtDecoder);
        OidcUser oidcUser = oidcUserService.loadUser(userRequest());

        assertTrue(oidcUser.getAuthorities().isEmpty());
    }

    @Test
    void oidcUserService_resourceAccessWithoutMatchingClientId_resultsInNoAuthorities() {
        Jwt jwt = Jwt.withTokenValue("access-token-value")
                .header("alg", "none")
                .claim("resource_access", Map.of("other-client", Map.of("roles", List.of("admin"))))
                .build();
        when(jwtDecoder.decode("access-token-value")).thenReturn(jwt);

        OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = securityConfig.oidcUserService(jwtDecoder);
        OidcUser oidcUser = oidcUserService.loadUser(userRequest());

        assertTrue(oidcUser.getAuthorities().isEmpty());
    }

    @Test
    void oidcUserService_buildsUserInfoFromIdTokenClaimsOnly() {
        Jwt jwt = Jwt.withTokenValue("access-token-value")
                .header("alg", "none")
                .claim("sub", "user-1")
                .build();
        when(jwtDecoder.decode("access-token-value")).thenReturn(jwt);

        OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService = securityConfig.oidcUserService(jwtDecoder);
        OidcUser oidcUser = oidcUserService.loadUser(userRequest());

        assertEquals(Map.of("sub", "user-1"), oidcUser.getUserInfo().getClaims());
        assertEquals("id-token-value", oidcUser.getIdToken().getTokenValue());
    }
}
