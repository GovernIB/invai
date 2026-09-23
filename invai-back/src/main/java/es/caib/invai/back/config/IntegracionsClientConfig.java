package es.caib.invai.back.config;

import io.netty.channel.ChannelOption;
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

/**
 * Configures the {@link WebClient} used by this module's {@code rest.*} clients (Soffid,
 * DIR3CAIB) to call the {@code invai-api-interna} module, forwarding the current session's OIDC
 * ID token as a Bearer token so the callee's OAuth2 Resource Server can authorize the request.
 * <p>
 * Depends on there being an authenticated OIDC session on the calling thread: every current caller
 * runs inside an already-authenticated HTTP request, never in a background/{@code @Scheduled}
 * context, so this is not a practical restriction today.
 * </p>
 *
 * @since 1.0.4
 */
@Configuration
public class IntegracionsClientConfig {

    /** Base URL of the {@code invai-api-interna} module. */
    @Value("${es.caib.invai.integracions.base-url}")
    private String baseUrl;

    /**
     * Builds the {@link WebClient} used by {@code SoffidClient}/{@code Dir3CaibClient},
     * pre-configured with the {@code invai-api-interna} base URL, explicit connect/read timeouts,
     * and automatic Bearer token forwarding.
     *
     * @return the configured {@link WebClient}
     */
    @Bean
    public WebClient integracionsWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(bearerTokenFilter())
                .build();
    }

    /**
     * Forwards the current session's OIDC ID token as an {@code Authorization: Bearer} header on
     * every outbound request, so {@code invai-api-interna} can validate it against the same
     * {@code jwk-set-uri}. Leaves the request untouched when there is no authenticated OIDC
     * session, letting the callee reject it as unauthenticated.
     *
     * @return the configured {@link ExchangeFilterFunction}
     */
    private ExchangeFilterFunction bearerTokenFilter() {
        return (request, next) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof OidcUser oidcUser) {
                ClientRequest authorized = ClientRequest.from(request)
                        .headers(headers -> headers.setBearerAuth(oidcUser.getIdToken().getTokenValue()))
                        .build();
                return next.exchange(authorized);
            }
            return next.exchange(request);
        };
    }
}
