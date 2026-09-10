package es.caib.invai.back.config;

import io.netty.channel.ChannelOption;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Outbound HTTP client configuration for the Soffid SCIM 2.0 API, used to search CAIB personnel
 * by name and feed the "assign a responsible/authorized person" typeahead. Also holds the SCIM
 * endpoint paths (exposed via generated getters) so callers such as {@link
 * es.caib.invai.back.rest.soffid.SoffidClientAdapter} build request URIs from configuration
 * rather than hardcoded constants, mirroring how {@link #soffidWebClient()} itself is built from
 * the configured base URL.
 *
 * @since 1.0.4
 */
@Configuration
public class SoffidConfig {

    /** Base URL of the Soffid host (e.g. {@code https://proves.caib.es}), without the SCIM API path. */
    @Value("${es.caib.invai.soffid.base-url:}")
    private String baseUrl;

    /** Username used to authenticate against the Soffid SCIM 2.0 API via HTTP Basic Auth. */
    @Value("${es.caib.invai.soffid.username:}")
    private String username;

    /** Password used to authenticate against the Soffid SCIM 2.0 API via HTTP Basic Auth. */
    @Value("${es.caib.invai.soffid.password:}")
    private String password;

    /** SCIM 2.0 users search endpoint path, relative to {@link #baseUrl}. */
    @Getter
    @Value("${es.caib.invai.soffid.user-search-path:/soffid/webservice/scim2/v1/User/}")
    private String searchPath;

    /** SCIM 2.0 roles search endpoint path, relative to {@link #baseUrl}. */
    @Getter
    @Value("${es.caib.invai.soffid.role-search-path:/soffid/webservice/scim2/v1/Role/}")
    private String roleSearchPath;

    /**
     * Builds the {@link WebClient} instance used to call the Soffid SCIM 2.0 API, pre-configured
     * with the base URL and HTTP Basic Auth credentials, and explicit connect/read timeouts to
     * avoid indefinitely blocking on an unreachable or slow external system.
     *
     * @return the configured {@link WebClient} bean
     */
    @Bean
    public WebClient soffidWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(h -> h.setBasicAuth(username, password))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
