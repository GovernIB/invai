package es.caib.invai.back.config;

import es.caib.invai.back.exception.UserSessionTokenException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserSessionTokenProvider}: it hands out the user's access token (as renewed
 * by the Spring client manager) and reports, as {@link UserSessionTokenException}, every way in which
 * that is no longer possible.
 */
class UserSessionTokenProviderTest {

    private final OAuth2AuthorizedClientManager manager = mock(OAuth2AuthorizedClientManager.class);
    private final OAuth2AuthorizedClientService clientService = mock(OAuth2AuthorizedClientService.class);
    private final UserSessionTokenProvider provider = new UserSessionTokenProvider(manager, clientService);
    private final Authentication login = new UsernamePasswordAuthenticationToken("jperez", null, List.of());

    private static ClientRegistration registration() {
        return ClientRegistration.withRegistrationId("soffid")
                .clientId("client")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/login")
                .authorizationUri("http://idp/auth")
                .tokenUri("http://idp/token")
                .build();
    }

    private static OAuth2AccessToken accessToken(Instant expiresAt) {
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "access-token", expiresAt.minusSeconds(300), expiresAt);
    }

    private static OAuth2AuthorizedClient client(Instant expiresAt, boolean withRefresh) {
        OAuth2RefreshToken refresh = withRefresh ? new OAuth2RefreshToken("refresh-token", Instant.now()) : null;
        return new OAuth2AuthorizedClient(registration(), "jperez", accessToken(expiresAt), refresh);
    }

    @Test
    void canRenew_clientWithRefreshToken_isTrue() {
        when(clientService.loadAuthorizedClient("soffid", "jperez")).thenReturn(client(Instant.now().plusSeconds(60), true));

        assertTrue(provider.canRenew(login));
    }

    @Test
    void canRenew_clientWithoutRefreshToken_isFalse() {
        when(clientService.loadAuthorizedClient("soffid", "jperez")).thenReturn(client(Instant.now().plusSeconds(60), false));

        assertFalse(provider.canRenew(login));
    }

    @Test
    void canRenew_noAuthorizedClient_isFalse() {
        when(clientService.loadAuthorizedClient("soffid", "jperez")).thenReturn(null);

        assertFalse(provider.canRenew(login));
    }

    @Test
    void getAccessToken_returnsTheTokenTheManagerProvides() {
        when(manager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(client(Instant.now().plusSeconds(120), true));

        assertEquals("access-token", provider.getAccessToken(login));
    }

    @Test
    void getAccessToken_renewalRejectedByTheIdp_throws() {
        when(manager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenThrow(new OAuth2AuthorizationException(new OAuth2Error("invalid_grant")));

        assertThrows(UserSessionTokenException.class, () -> provider.getAccessToken(login));
    }

    @Test
    void getAccessToken_noAuthorizedClientAnyMore_throws() {
        when(manager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(null);

        assertThrows(UserSessionTokenException.class, () -> provider.getAccessToken(login));
    }

    @Test
    void getAccessToken_tokenExpiredAndNotRenewed_throws() {
        when(manager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(client(Instant.now().minusSeconds(10), false));

        assertThrows(UserSessionTokenException.class, () -> provider.getAccessToken(login));
    }
}
