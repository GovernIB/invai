package es.caib.invai.back.interna.auth;

import es.caib.invai.back.interna.auth.DTO.UserAuthDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthController}, verifying identity resolution from the current
 * security {@link Principal} and the session/cookie clearance logic performed on logout.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private Principal principal;

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
        when(principal.getName()).thenReturn("jdoe");

        ResponseEntity<?> response = authController.getAuthInformation(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof UserAuthDTO);

        UserAuthDTO body = (UserAuthDTO) response.getBody();
        assertTrue(body.isAuthenticated());
        assertEquals("jdoe", body.getUsername());
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
        HttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<Void> result = authController.logout(request, response);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        Cookie clearedCookie = ((MockHttpServletResponse) response).getCookie("JSESSIONID");
        assertNotNull(clearedCookie);
        assertEquals("/", clearedCookie.getPath());
    }
}
