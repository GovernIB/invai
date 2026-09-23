package es.caib.invai.back.interna.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Public authentication routing controller.
 * Acts as an entry point gateway to trigger secure OAuth2/OIDC authentication protocols.
 *
 * @since 1.0.1
 */
@Tag(name = "Inici de sessió", description = "Servei d'entrada per iniciar el flux d'autenticació OAuth2/OIDC amb el proveïdor Soffid.")
@RestController
@RequestMapping("api/auth")
public class LoginController {

    /**
     * Intercepts login requests and initiates an HTTP 302 redirection to this deployment's own
     * Spring Security OpenID Connect filter endpoint associated with the corporate Soffid provider.
     * Built from {@link HttpServletRequest#getContextPath()} rather than a hardcoded literal, since
     * this module's own context-root (currently {@code /invaiback}) is not the same as
     * {@code invai-api-interna}'s.
     *
     * @param request  the underlying servlet request, used to resolve this deployment's context-root
     * @param response the underlying servlet context wrapper {@link HttpServletResponse}
     * @throws IOException if an error occurs during the redirection dispatch
     */
    @GetMapping("/login")
    public void redirectToSoffid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/oauth2/authorization/soffid");
    }
}
