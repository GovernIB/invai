package es.caib.invai.back.interna.auth;

import es.caib.invai.back.interna.auth.DTO.UserAuthDTO;
import es.caib.invai.back.interna.auth.DTO.UserPermissionsDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AuthController}, verifying identity resolution from the current
 * security {@link Principal} and the session/cookie clearance logic performed on logout.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAuthInformation_nullPrincipal_returnsUnauthorized() {
        ResponseEntity<?> response = authController.getAuthInformation(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getAuthInformation_authenticatedPrincipal_returnsOkWithUserAuthDTO() {
        OidcIdToken idToken = new OidcIdToken("token-value", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("sub", "jdoe", "name", "John Doe"));
        OidcUserInfo userInfo = new OidcUserInfo(Map.of("sub", "jdoe", "name", "John Doe"));
        DefaultOidcUser oidcUser = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_INV_SUPER")), idToken, userInfo);
        OAuth2AuthenticationToken principal = new OAuth2AuthenticationToken(oidcUser, oidcUser.getAuthorities(), "soffid");

        ResponseEntity<?> response = authController.getAuthInformation(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(UserAuthDTO.class, response.getBody());

        UserAuthDTO body = (UserAuthDTO) response.getBody();
        assertTrue(body.isAuthenticated());
        assertEquals("jdoe", body.getUsername());
        assertEquals("John Doe", body.getFullName());
    }

    private static OAuth2AuthenticationToken loginWithRoles(String... roles) {
        OidcIdToken idToken = new OidcIdToken("token-value", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("sub", "jdoe", "name", "John Doe"));
        OidcUserInfo userInfo = new OidcUserInfo(Map.of("sub", "jdoe", "name", "John Doe"));
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles).map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
        DefaultOidcUser oidcUser = new DefaultOidcUser(authorities, idToken, userInfo);
        OAuth2AuthenticationToken principal = new OAuth2AuthenticationToken(oidcUser, oidcUser.getAuthorities(), "soffid");
        SecurityContextHolder.getContext().setAuthentication(principal);
        return principal;
    }

    @Test
    void getAuthInformation_superUser_hasEveryPermission() {
        ResponseEntity<?> response = authController.getAuthInformation(loginWithRoles("INV_SUPER"));

        UserPermissionsDTO permissions = ((UserAuthDTO) response.getBody()).getPermissions();
        assertTrue(permissions.isSystems() && permissions.isDevelopment() && permissions.isSecurity() && permissions.isData()
                && permissions.isAccessibility() && permissions.isIntegration() && permissions.isDocumentation()
                && permissions.isDir3() && permissions.isResponsibleRead() && permissions.isResponsibleWrite() && permissions.isSuperUser());
    }

    @Test
    void getAuthInformation_wsUser_hasIntegrationDocumentationAndDir3Only() {
        ResponseEntity<?> response = authController.getAuthInformation(loginWithRoles("INV_WS"));

        UserPermissionsDTO permissions = ((UserAuthDTO) response.getBody()).getPermissions();
        assertTrue(permissions.isIntegration());
        assertTrue(permissions.isDocumentation());
        assertTrue(permissions.isDir3());
        assertFalse(permissions.isSystems());
        assertFalse(permissions.isDevelopment());
        assertFalse(permissions.isSecurity());
        assertFalse(permissions.isData());
        assertFalse(permissions.isAccessibility());
        assertFalse(permissions.isResponsibleRead());
        assertFalse(permissions.isResponsibleWrite());
        assertFalse(permissions.isSuperUser());
    }

    @Test
    void getAuthInformation_securityUser_readsAndWritesTheResponsibles() {
        UserPermissionsDTO permissions = ((UserAuthDTO) authController.getAuthInformation(loginWithRoles("INV_SEGURETAT")).getBody()).getPermissions();

        assertTrue(permissions.isSecurity());
        assertTrue(permissions.isResponsibleRead());
        assertTrue(permissions.isResponsibleWrite());
        assertFalse(permissions.isSystems());
    }

    @Test
    void getAuthInformation_dataUser_onlyReadsTheResponsibles() {
        UserPermissionsDTO permissions = ((UserAuthDTO) authController.getAuthInformation(loginWithRoles("INV_DADES")).getBody()).getPermissions();

        assertTrue(permissions.isData());
        assertTrue(permissions.isResponsibleRead());
        assertFalse(permissions.isResponsibleWrite());
    }

    @Test
    void getAuthInformation_plainUser_hasOnlyDocumentationAndDir3() {
        ResponseEntity<?> response = authController.getAuthInformation(loginWithRoles("INV_USER"));

        UserPermissionsDTO permissions = ((UserAuthDTO) response.getBody()).getPermissions();
        assertTrue(permissions.isDocumentation());
        assertTrue(permissions.isDir3());
        assertFalse(permissions.isSystems() || permissions.isDevelopment() || permissions.isSecurity() || permissions.isData()
                || permissions.isAccessibility() || permissions.isIntegration() || permissions.isResponsibleRead()
                || permissions.isResponsibleWrite() || permissions.isSuperUser());
    }

    @Test
    void logout_withActiveAuthenticationAndSession_invalidatesSessionAndClearsCookie() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "jdoe", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/invaiapi");
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<Void> result = authController.logout(request, response);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        // Session is invalidated by SecurityContextLogoutHandler before AuthController's own
        // (now redundant/no-op) request.getSession(false) + invalidate() call runs - since
        // MockHttpServletRequest.getSession(false) nulls out the reference once invalidated.
        assertTrue(session.isInvalid());

        Cookie clearedCookie = response.getCookie("JSESSIONID");
        assertNotNull(clearedCookie);
        assertEquals(0, clearedCookie.getMaxAge());
        assertEquals("/invaiapi", clearedCookie.getPath());
        assertTrue(clearedCookie.isHttpOnly());
    }

    @Test
    void logout_noActiveSessionOrAuthentication_stillClearsCookieWithRootPath() {
        // No authentication set in SecurityContextHolder, and request.getSession(false) returns null.
        HttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<Void> result = authController.logout(request, response);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        Cookie clearedCookie = response.getCookie("JSESSIONID");
        assertNotNull(clearedCookie);
        assertEquals("/", clearedCookie.getPath());
    }
}
