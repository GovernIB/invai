package es.caib.invai.back.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Objects;

/**
 * Configures the {@link WebClient} used by this module's {@code rest.*} clients (Soffid, DIR3CAIB,
 * Open Data) to call the {@code invai-api-interna} module, forwarding the current session's OIDC
 * ID token as a Bearer token so the callee's OAuth2 Resource Server can authorize the request.
 * <p>
 * Requests made inside an authenticated HTTP request carry the user's token. Background jobs
 * (identified by a {@link BackgroundJobAuthentication} on their thread) have no session, so they
 * carry the access token of the user who launched them, renewed as it expires (see
 * {@link UserSessionTokenProvider}). Anything else is sent without credentials and rejected by
 * the callee.
 * </p>
 *
 * @since 1.0.5
 */
@Configuration
public class IntegracionsClientConfig {

    /** Base URL of the {@code invai-api-interna} module. */
    @Value("${es.caib.invai.integracions.base-url}")
    private String baseUrl;

    /** Maximum time, in seconds, to connect to {@code invai-api-interna}. */
    @Value("${es.caib.invai.integracions.connect-timeout-seconds}")
    private long connectTimeoutSeconds;

    /**
     * Maximum time, in seconds, to wait for {@code invai-api-interna} to answer. It must stay above
     * the longest timeout {@code invai-api-interna} applies to what it calls (see {@code
     * es.caib.invai.soffid.request-timeout-seconds}), so its answer, a 504 when that one is
     * exceeded, arrives before this one fires.
     */
    @Value("${es.caib.invai.integracions.response-timeout-seconds}")
    private long responseTimeoutSeconds;

    /** Source of the launching user's renewable token, used by background jobs. */
    @Autowired
    private UserSessionTokenProvider userSessionTokenProvider;

    /**
     * Builds the {@link WebClient} used by {@code SoffidClient}/{@code Dir3CaibClient}/
     * {@code DataApiClient}, pre-configured with the {@code invai-api-interna} base URL, explicit
     * connect/read timeouts, and automatic Bearer token forwarding.
     *
     * @return the configured {@link WebClient}
     */
    @Bean
    public WebClient integracionsWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeoutSeconds * 1000))
                .responseTimeout(Duration.ofSeconds(responseTimeoutSeconds));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(bearerTokenFilter())
                .build();
    }

    /**
     * Adds an {@code Authorization: Bearer} header to every outbound request so
     * {@code invai-api-interna} can validate it against the same {@code jwk-set-uri}: the current
     * session's OIDC ID token for a user request, or the launching user's access token (renewed
     * when about to expire) when the calling thread runs a background job. Leaves the request
     * untouched otherwise, letting the callee reject it as unauthenticated.
     *
     * @return the configured {@link ExchangeFilterFunction}
     */
    ExchangeFilterFunction bearerTokenFilter() {
        return (request, next) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof BackgroundJobAuthentication job) {
                String userToken = userSessionTokenProvider.getAccessToken(job.getUserAuthentication());
                ClientRequest authorized = ClientRequest.from(request)
                        .headers(headers -> headers.setBearerAuth(userToken))
                        .build();
                return next.exchange(authorized);
            }
            if (Objects.nonNull(auth) && auth.getPrincipal() instanceof OidcUser oidcUser) {
                ClientRequest authorized = ClientRequest.from(request)
                        .headers(headers -> headers.setBearerAuth(oidcUser.getIdToken().getTokenValue()))
                        .build();
                return next.exchange(authorized);
            }
            return next.exchange(request);
        };
    }
}
