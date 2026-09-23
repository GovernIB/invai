package es.caib.invai.back.interna.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link LoginController}, verifying that the public login entry point
 * issues the expected HTTP redirect to this deployment's own OAuth2/OIDC filter endpoint.
 */
class LoginControllerTest {

    private LoginController loginController;

    @BeforeEach
    void setUp() {
        loginController = new LoginController();
    }

    @Test
    void redirectToSoffid_sendsRedirectToOauth2AuthorizationEndpointUnderOwnContextPath() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/invaiback");
        MockHttpServletResponse response = new MockHttpServletResponse();

        loginController.redirectToSoffid(request, response);

        assertEquals("/invaiback/oauth2/authorization/soffid", response.getRedirectedUrl());
        assertEquals(302, response.getStatus());
    }
}
