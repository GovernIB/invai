package es.caib.invai.api.externa;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Public authentication routing controller.
 * Acts as an entry point gateway to trigger secure OAuth2/OIDC authentication protocols.
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("externa/api/auth")
public class LoginController {

    /**
     * Intercepts login requests and initiates an HTTP 302 redirection to the internal
     * Spring Security OpenID Connect filter endpoint associated with the corporate Soffid provider.
     *
     * @param response the underlying servlet context wrapper {@link HttpServletResponse}
     * @throws IOException if an error occurs during the redirection dispatch
     */
    @GetMapping("/login")
    public void redirectToSoffid(HttpServletResponse response) throws IOException {
        response.sendRedirect("/invaiapi/oauth2/authorization/soffid");
    }
}