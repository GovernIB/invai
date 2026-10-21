package es.caib.invai.api.interna.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configures what is needed to query the URLs an application publishes for its "Open Data" tab
 * (its own external REST API) and its "Reutilització" tab (its own backend) - see {@code
 * es.caib.invai.api.interna.rest.data.DataApiClient}: the {@link WebClient} bean.
 *
 * @since 1.0.5
 */
@Configuration
public class DataApiConfig {

    /** Maximum time, in seconds, to connect to the URL being checked. */
    @Value("${es.caib.invai.data.connect-timeout-seconds}")
    private long connectTimeoutSeconds;

    /** Maximum time, in seconds, to wait for the URL being checked to answer. */
    @Value("${es.caib.invai.data.response-timeout-seconds}")
    private long responseTimeoutSeconds;

    /**
     * Builds the {@link WebClient} with explicit connect/read timeouts. No base URL is configured -
     * every call targets a fully qualified absolute URL. Redirects are followed, since an
     * application's swagger is often served from a path that redirects to its final one, and it is
     * the final status code that says whether the URL works.
     *
     * @return the configured {@link WebClient}
     */
    @Bean
    public WebClient dataWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeoutSeconds * 1000))
                .responseTimeout(Duration.ofSeconds(responseTimeoutSeconds))
                .followRedirect(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
