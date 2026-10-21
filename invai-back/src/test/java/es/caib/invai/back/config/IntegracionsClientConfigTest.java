package es.caib.invai.back.config;

import es.caib.invai.back.exception.UserSessionTokenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the Bearer token filter of {@link IntegracionsClientConfig}: user requests forward
 * the session's ID token, background jobs use the launching user's (renewed) access token, and anything
 * else goes out without credentials.
 */
class IntegracionsClientConfigTest {

    private final UserSessionTokenProvider tokenProvider = mock(UserSessionTokenProvider.class);
    private final IntegracionsClientConfig config = new IntegracionsClientConfig();

    IntegracionsClientConfigTest() {
        ReflectionTestUtils.setField(config, "userSessionTokenProvider", tokenProvider);
        ReflectionTestUtils.setField(config, "connectTimeoutSeconds", 5L);
        ReflectionTestUtils.setField(config, "responseTimeoutSeconds", 10L);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /** Runs a request through the filter and returns the request that reached the next step. */
    private ClientRequest filtered() {
        AtomicReference<ClientRequest> sent = new AtomicReference<>();
        ExchangeFilterFunction filter = config.bearerTokenFilter();
        filter.filter(ClientRequest.create(HttpMethod.GET, URI.create("http://integracions.local/soffid/users")).build(),
                request -> {
                    sent.set(request);
                    return Mono.empty();
                }).block();
        return sent.get();
    }

    @Test
    void userRequest_forwardsTheSessionIdToken() {
        OidcIdToken idToken = new OidcIdToken("user-id-token", Instant.now(), Instant.now().plusSeconds(60), Map.of("sub", "u1"));
        DefaultOidcUser user = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_INV_SUPER")), idToken);
        SecurityContextHolder.getContext().setAuthentication(
                new OAuth2AuthenticationToken(user, user.getAuthorities(), "soffid"));

        ClientRequest sent = filtered();

        assertEquals("Bearer user-id-token", sent.headers().getFirst(HttpHeaders.AUTHORIZATION));
        verify(tokenProvider, never()).getAccessToken(any());
    }

    @Test
    void backgroundJob_usesTheLaunchingUsersToken() {
        Authentication login = new UsernamePasswordAuthenticationToken("jperez", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(new BackgroundJobAuthentication("jperez", login));
        when(tokenProvider.getAccessToken(login)).thenReturn("user-access-token");

        ClientRequest sent = filtered();

        assertEquals("Bearer user-access-token", sent.headers().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void backgroundJob_tokenUnavailable_failsTheRequestInsteadOfSendingItAnonymously() {
        Authentication login = new UsernamePasswordAuthenticationToken("jperez", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(new BackgroundJobAuthentication("jperez", login));
        when(tokenProvider.getAccessToken(login)).thenThrow(new UserSessionTokenException("session gone"));

        assertThrows(UserSessionTokenException.class, this::filtered);
    }

    @Test
    void noAuthentication_isSentWithoutCredentials() {
        ClientRequest sent = filtered();

        assertNull(sent.headers().getFirst(HttpHeaders.AUTHORIZATION));
        verify(tokenProvider, never()).getAccessToken(any());
    }

    @Test
    void otherKindOfAuthentication_doesNotGetAnyBackgroundToken() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("someone", null, List.of()));

        ClientRequest sent = filtered();

        assertNull(sent.headers().getFirst(HttpHeaders.AUTHORIZATION));
        verify(tokenProvider, never()).getAccessToken(any());
    }

    @Test
    void backgroundJobAuthentication_exposesTheLaunchingUsernameAsPrincipalAndKeepsTheLogin() {
        Authentication login = new UsernamePasswordAuthenticationToken("jperez", null, List.of());
        BackgroundJobAuthentication authentication = new BackgroundJobAuthentication("jperez", login);

        assertEquals("jperez", authentication.getPrincipal());
        assertSame(login, authentication.getUserAuthentication());
        assertEquals(true, authentication.isAuthenticated());
        assertNull(authentication.getCredentials());
    }
}
