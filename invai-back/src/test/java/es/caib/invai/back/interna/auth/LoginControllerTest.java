package es.caib.invai.back.interna.auth;

import es.caib.invai.back.interna.auth.LoginController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link LoginController}, verifying that the public login entry point
 * issues the expected HTTP redirect to the internal OAuth2/OIDC filter endpoint.
 */
class LoginControllerTest {

    private LoginController loginController;

    @BeforeEach
    void setUp() {
        loginController = new LoginController();
    }

    @Test
    void redirectToSoffid_sendsRedirectToOauth2AuthorizationEndpoint() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        loginController.redirectToSoffid(response);

        assertEquals("/invaiapi/interna/oauth2/authorization/soffid", response.getRedirectedUrl());
        assertEquals(302, response.getStatus());
    }
}
