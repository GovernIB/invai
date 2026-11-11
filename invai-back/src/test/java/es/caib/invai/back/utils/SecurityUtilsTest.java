package es.caib.invai.back.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the role-checking helpers of {@link SecurityUtils}.
 */
class SecurityUtilsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void loginWithRoles(String... roles) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "jdoe", null, java.util.Arrays.stream(roles).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList()));
    }

    @Test
    void hasAccess_nobodyLoggedIn_returnsFalse() {
        assertFalse(SecurityUtils.hasAccess("hasRole('INV_SUPER')"));
    }

    @Test
    void hasAccess_userWithOneOfTheRoles_returnsTrue() {
        loginWithRoles("INV_WS");

        assertTrue(SecurityUtils.hasAccess("hasAnyRole('INV_SUPER','INV_WS')"));
        assertTrue(SecurityUtils.hasAccess("hasRole('INV_WS')"));
    }

    @Test
    void hasAccess_userWithoutTheRoles_returnsFalse() {
        loginWithRoles("INV_USER");

        assertFalse(SecurityUtils.hasAccess("hasAnyRole('INV_SUPER','INV_WS')"));
    }
}
