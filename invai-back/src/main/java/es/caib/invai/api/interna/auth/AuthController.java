package es.caib.invai.api.interna.auth;

import es.caib.invai.api.interna.auth.DTO.UserAuthDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;

/**
 * Internal REST controller handling session state and identity verification checks.
 * Exposes endpoints used by client applications to verify the current context profile authentication data.
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("interna/auth")
@Slf4j
public class AuthController {

    /**
     * Resolves and extracts identity credentials bound to the current execution thread context.
     * Evaluates security principal structures to build a standard session response.
     *
     * @param principal the authenticated security token instance injected by Spring Security framework
     * @return a {@link ResponseEntity} wrapping the mapped {@link UserAuthDTO} with an HTTP 200 OK status,
     * or an empty payload with an HTTP 401 Unauthorized status if no active security context exists
     */
    @GetMapping("/me")
    public ResponseEntity<?> getAuthInformation(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserAuthDTO userAuth = new UserAuthDTO();
        userAuth.setAuthenticated(true);
        userAuth.setUsername(principal.getName());

        return ResponseEntity.ok(userAuth);
    }

    /**
     * revokes the active session, clears Spring Security context, and clear the JSESSIONID cookie.
     *
     * @param request  the current HTTP servlet request containing the active session
     * @param response the HTTP servlet response to append cookie clearance mutations
     * @return a state mapping signaling successful logout execution
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Auth: Processing logout request for current authenticated user");

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);

        log.debug("Auth: Session invalidated and JSESSIONID cookie marked for removal");
        return ResponseEntity.noContent().build();
    }
}