package es.caib.invai.back.config;

import es.caib.invai.back.exception.UserSessionTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

/**
 * Gives a background job the access token of the user who launched it, renewing it with the
 * refresh token of that user's login whenever it is about to expire. Spring keeps, in memory and
 * keyed by user, the authorized client created when the user logged in; the job has no HTTP request
 * (so no session) to read it from, but can look it up by user from there. The renewal only works
 * while the user's login session is still alive at the identity provider (not logged out, within the
 * provider's session lifetime) and the provider issued a refresh token at login.
 *
 * @since 1.0.6
 */
@Component
public class UserSessionTokenProvider {

    /** Id of the OAuth2 client registration the users log in with. */
    static final String REGISTRATION_ID = "soffid";

    /** Loads the user's authorized client and refreshes its access token when it is about to expire. */
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    /** Holds the authorized client of every logged-in user, refresh token included. */
    private final OAuth2AuthorizedClientService authorizedClientService;

    /**
     * @param clientRegistrationRepository registered OAuth2 clients
     * @param authorizedClientService      store of the users' authorized clients
     */
    @Autowired
    public UserSessionTokenProvider(ClientRegistrationRepository clientRegistrationRepository,
                                    OAuth2AuthorizedClientService authorizedClientService) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().refreshToken().build());
        this.authorizedClientManager = manager;
        this.authorizedClientService = authorizedClientService;
    }

    UserSessionTokenProvider(OAuth2AuthorizedClientManager authorizedClientManager,
                             OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientManager = authorizedClientManager;
        this.authorizedClientService = authorizedClientService;
    }

    /**
     * @param userAuthentication the login authentication of the user
     * @return {@code true} if that user's login has a refresh token, i.e. its token can be renewed
     * for as long as the login session lasts at the identity provider
     */
    public boolean canRenew(Authentication userAuthentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(REGISTRATION_ID, userAuthentication.getName());
        return Objects.nonNull(client) && Objects.nonNull(client.getRefreshToken());
    }

    /**
     * Returns a valid access token of the user, renewing it first when it is about to expire.
     *
     * @param userAuthentication the login authentication of the user
     * @return the access token
     * @throws UserSessionTokenException if the user has no authorized client anymore, or their
     *                                   token has expired and couldn't be renewed
     */
    public String getAccessToken(Authentication userAuthentication) {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId(REGISTRATION_ID)
                .principal(userAuthentication)
                .build();
        OAuth2AuthorizedClient client;
        try {
            client = authorizedClientManager.authorize(request);
        } catch (OAuth2AuthorizationException ex) {
            throw new UserSessionTokenException("The user's token could not be renewed", ex);
        }
        if (Objects.isNull(client) || Objects.isNull(client.getAccessToken())) {
            throw new UserSessionTokenException("The user has no authorized session any more");
        }
        Instant expiresAt = client.getAccessToken().getExpiresAt();
        if (Objects.nonNull(expiresAt) && expiresAt.isBefore(Instant.now())) {
            throw new UserSessionTokenException("The user's token has expired and could not be renewed");
        }
        return client.getAccessToken().getTokenValue();
    }
}
