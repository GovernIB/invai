package es.caib.invai.back.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configures the {@link WebClient} bean used to call the external DIR3CAIB service (the CAIB's
 * implementation of Spain's Directorio Común, a catalog of administrative organizational units).
 * <p>
 * Built on {@code WebClient} (rather than the simpler synchronous {@code RestClient}) so future
 * outbound integrations in this codebase can reuse a non-blocking client without a rework, even
 * though the current {@code Dir3CaibClientAdapter} still blocks for a synchronous result to fit
 * this codebase's otherwise fully blocking Spring MVC call chain.
 * </p>
 *
 * @since 1.0.4
 */
@Configuration
public class Dir3CaibConfig {

    /**
     * Base URL of the DIR3CAIB REST API. Defaults to empty since no real DIR3CAIB instance is
     * reachable yet — keeps the application context bootable; calls will simply fail with
     * {@code ERR_ADMUNIT_DIR3_UNAVAILABLE} until this is configured with a real value.
     */
    @Value("${es.caib.invai.dir3caib.base-url:}")
    private String dir3CaibBaseUrl;

    /** Application username used to authenticate against DIR3CAIB via HTTP Basic Auth. */
    @Value("${es.caib.invai.dir3caib.username:}")
    private String dir3CaibUsername;

    /** Application password used to authenticate against DIR3CAIB via HTTP Basic Auth. */
    @Value("${es.caib.invai.dir3caib.password:}")
    private String dir3CaibPassword;

    /**
     * Builds the {@link WebClient} used by {@code Dir3CaibClientAdapter}, pre-configured with the
     * DIR3CAIB base URL, HTTP Basic Auth credentials, and explicit connect/read timeouts.
     *
     * @return the configured {@link WebClient}
     */
    @Bean
    public WebClient dir3CaibWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10));

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(dir3CaibBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeaders(headers -> headers.setBasicAuth(dir3CaibUsername, dir3CaibPassword))
                .exchangeStrategies(exchangeStrategies)
                .build();
    }
}
