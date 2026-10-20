package es.caib.invai.api.interna.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

/**
 * Configures the {@link WebClient} bean shared by both the "Open Data" tab (an application's own
 * external REST API) and the "Reutilització" tab (its own backend) - see {@code
 * es.caib.invai.api.interna.rest.data.DataApiClient}. Both tabs target the same per-application
 * host ({@link #baseUrl}); only the resolved path segment differs ({@link #openDataPathSuffixes}/
 * {@link #reusePathSuffixes}), prefixed by that application's own {@code name} at call time rather
 * than being fixed for the whole client. Real CAIB applications don't all publish their OpenAPI
 * document under the same convention, so each list carries every known convention as a candidate -
 * {@code DataApiClient} races all of them and keeps whichever responds first.
 *
 * @since 1.0.5
 */
@Configuration
public class DataApiConfig {

    /**
     * Base host serving every application's external REST API and its own backend. Defaults to
     * the real CAIB intranet host, since - unlike Soffid/DIR3CAIB - this integration targets a
     * fixed, publicly documented internal host rather than an environment-specific one.
     */
    @Value("${es.caib.invai.data.base-url}")
    private String baseUrl;

    /**
     * Fixed segment shared by every OpenAPI document candidate (e.g. {@code api/externa/}),
     * appended directly (no separating slash) after an application's own {@code name}, before
     * {@link #openDataPathSuffixes}.
     */
    @Value("${es.caib.invai.data.opendata.path-prefix}")
    private String openDataPathPrefix;

    /**
     * Candidate file names (comma-separated in the underlying property), each appended after
     * {@link #openDataPathPrefix} to build every candidate OpenAPI document URL to race - e.g., for
     * {@code name=sedeib}, prefix {@code api/externa/} and suffix {@code openapi.json}, the
     * resolved candidate is {@code /sedeibapi/externa/openapi.json}.
     */
    @Value("${es.caib.invai.data.opendata.path-suffixes}")
    private List<String> openDataPathSuffixes;

    /**
     * Fixed segment shared by every reuse document candidate (e.g. {@code api/interna/}), appended
     * directly (no separating slash) after an application's own {@code name}, before {@link
     * #reusePathSuffixes}.
     */
    @Value("${es.caib.invai.data.reuse.path-prefix}")
    private String reusePathPrefix;

    /**
     * Candidate file names (comma-separated in the underlying property), each appended after
     * {@link #reusePathPrefix} to build every candidate reuse document URL to race.
     */
    @Value("${es.caib.invai.data.reuse.path-suffixes}")
    private List<String> reusePathSuffixes;

    /**
     * Builds every candidate OpenAPI document URL for the given application name - pure string
     * concatenation, no network call. {@code DataApiClient} races all of them when a caller doesn't
     * ask to use its own explicit override URL instead.
     *
     * @param applicationName the {@code name} of the application whose OpenAPI document URL candidates to build
     * @return every candidate absolute URL, e.g. {@code https://intranet.caib.es/sedeibapi/externa/openapi.json}
     */
    public List<String> buildOpenApiUrlCandidates(String applicationName) {
        return openDataPathSuffixes.stream()
                .map(suffix -> baseUrl + "/" + applicationName + openDataPathPrefix + suffix)
                .toList();
    }

    /**
     * Builds every candidate reuse document URL for the given application name - pure string
     * concatenation, no network call. {@code DataApiClient} races all of them when a caller doesn't
     * ask to use its own explicit override URL instead.
     *
     * @param applicationName the {@code name} of the application whose reuse document URL candidates to build
     * @return every candidate absolute URL
     */
    public List<String> buildReuseUrlCandidates(String applicationName) {
        return reusePathSuffixes.stream()
                .map(suffix -> baseUrl + "/" + applicationName + reusePathPrefix + suffix)
                .toList();
    }

    /**
     * Builds the {@link WebClient} shared by both tabs, with explicit connect/read timeouts. No
     * base URL is configured - every call resolves a fully-qualified absolute URL first (see
     * {@link #buildOpenApiUrlCandidates}/{@link #buildReuseUrlCandidates}), since it may come from
     * either an application-name-derived candidate or a caller's own explicit override. Raises the
     * default in-memory buffer size, since these documents can be considerably larger than the
     * small JSON payloads Soffid/DIR3CAIB return.
     *
     * @return the configured {@link WebClient}
     */
    @Bean
    public WebClient dataWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10));

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(8 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .build();
    }
}
