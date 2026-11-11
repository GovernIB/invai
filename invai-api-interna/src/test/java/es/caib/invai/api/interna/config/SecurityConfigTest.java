package es.caib.invai.api.interna.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SecurityConfig}: Keycloak/Soffid client roles are extracted from the JWT
 * {@code resource_access} claim into {@code ROLE_} authorities, and the firewall tolerates semicolons.
 */
class SecurityConfigTest {

    private static final String CLIENT_ID = "invai-client";

    private JwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "clientId", CLIENT_ID);
        converter = config.jwtAuthenticationConverter();
    }

    private Jwt jwtWith(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "jdoe");
        claims.forEach(builder::claim);
        return builder.build();
    }

    private Set<String> authoritiesOf(Jwt jwt) {
        return converter.convert(jwt).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    @Test
    void jwtAuthenticationConverter_mapsClientRolesToRoleAuthorities() {
        Jwt jwt = jwtWith(Map.of("resource_access",
                Map.of(CLIENT_ID, Map.of("roles", List.of("INV_USER", "INV_SUPER")))));

        assertEquals(Set.of("ROLE_INV_USER", "ROLE_INV_SUPER"), authoritiesOf(jwt));
    }

    @Test
    void jwtAuthenticationConverter_withoutResourceAccessClaim_hasNoAuthorities() {
        assertTrue(authoritiesOf(jwtWith(Map.of())).isEmpty());
    }

    @Test
    void jwtAuthenticationConverter_withoutRolesForTheClient_hasNoAuthorities() {
        Jwt jwt = jwtWith(Map.of("resource_access", Map.of("other-client", Map.of("roles", List.of("X")))));

        assertTrue(authoritiesOf(jwt).isEmpty());
    }

    @Test
    void httpFirewall_allowsSemicolons() {
        StrictHttpFirewall firewall = new SecurityConfig().httpFirewall();

        assertNotNull(firewall);
        // A semicolon in the URL would be rejected if the firewall were left with its default.
        firewall.getFirewalledRequest(new org.springframework.mock.web.MockHttpServletRequest("GET", "/a;b"));
    }
}
