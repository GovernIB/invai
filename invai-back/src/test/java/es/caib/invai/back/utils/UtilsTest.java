package es.caib.invai.back.utils;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link Utils}, covering its reflective field sanitization routine and its
 * current-user resolution logic across every {@link SecurityUtils#getCurrentUser()} outcome.
 */
class UtilsTest {

    @Getter
    @Setter
    private static class SanitizeTarget {
        private String name;
        private String description;
        private Long id;
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sanitize_null_doesNothing() {
        Utils.sanitize(null);
    }

    @Test
    void sanitize_trimsAllStringFieldsAndLeavesOthersUntouched() {
        SanitizeTarget target = new SanitizeTarget();
        target.setName("  padded name  ");
        target.setDescription(null);
        target.setId(42L);

        Utils.sanitize(target);

        assertEquals("padded name", target.getName());
        assertNull(target.getDescription());
        assertEquals(42L, target.getId());
    }

    @Test
    void resolveCurrentUsername_unauthenticated_returnsSystemFallback() {
        SecurityContextHolder.clearContext();

        String result = Utils.resolveCurrentUsername();

        assertEquals("SYSTEM_USER", result);
    }

    @Test
    void resolveCurrentUsername_stringPrincipal_returnsPrincipalValue() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "jdoe", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = Utils.resolveCurrentUsername();

        assertEquals("jdoe", result);
    }

    @Test
    void resolveCurrentUsername_oidcPrincipalWithPreferredUsername_returnsClaimValue() {
        OidcIdToken idToken = new OidcIdToken("token-value", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("sub", "user-1"));
        OidcUserInfo userInfo = new OidcUserInfo(Map.of("preferred_username", "mmartin"));
        DefaultOidcUser oidcUser = new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), idToken, userInfo);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                oidcUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = Utils.resolveCurrentUsername();

        assertEquals("mmartin", result);
    }
}
