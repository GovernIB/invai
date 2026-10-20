package es.caib.invai.api.interna.rest.data;

import es.caib.invai.api.interna.config.DataApiConfig;
import es.caib.invai.api.interna.exception.IntegrationTimeoutException;
import es.caib.invai.api.interna.exception.IntegrationUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DataApiClient}. {@link WebClient} is wired with a stub
 * {@link ExchangeFunction} instead of a real {@code ClientHttpConnector}, so every test runs with
 * no real network access and no chained-mock setup of WebClient's fluent builder types.
 */
class DataApiClientTest {

    private static final String BASE_URL = "https://intranet.caib.es";
    private static final String PREFIX = "api/externa/";
    private static final List<String> SUFFIXES = List.of("swagger.json", "openapi.json", "api-docs.json");

    private DataApiClient buildClient(ExchangeFunction exchangeFunction) {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        DataApiConfig config = new DataApiConfig();
        ReflectionTestUtils.setField(config, "baseUrl", BASE_URL);
        ReflectionTestUtils.setField(config, "openDataPathPrefix", PREFIX);
        ReflectionTestUtils.setField(config, "openDataPathSuffixes", SUFFIXES);
        ReflectionTestUtils.setField(config, "reusePathPrefix", PREFIX);
        ReflectionTestUtils.setField(config, "reusePathSuffixes", SUFFIXES);

        DataApiClient client = new DataApiClient();
        ReflectionTestUtils.setField(client, "dataApiConfig", config);
        ReflectionTestUtils.setField(client, "dataWebClient", webClient);
        return client;
    }

    private static ClientResponse okDocument(String operationId) {
        return ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"paths\":{\"/services/centre\":{\"get\":{\"operationId\":\"" + operationId + "\"}}}}")
                .build();
    }

    @Test
    void getOpenApiDocumentForOpenData_noCache_racesEveryCandidateAndKeepsTheOneThatResponds() {
        Set<String> requested = ConcurrentHashMap.newKeySet();
        DataApiClient client = buildClient(request -> {
            requested.add(request.url().toString());
            if (request.url().toString().endsWith("openapi.json")) {
                return Mono.just(okDocument("centrePerEtapa"));
            }
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        });

        OpenApiDocumentResult result = client.getOpenApiDocumentForOpenData("sedeib", false, null);

        assertEquals("https://intranet.caib.es/sedeibapi/externa/openapi.json", result.getResolvedUrl());
        assertEquals("centrePerEtapa", result.getDocument().getPaths().get("/services/centre").getGet().getOperationId());
        assertTrue(requested.contains("https://intranet.caib.es/sedeibapi/externa/swagger.json"));
        assertTrue(requested.contains("https://intranet.caib.es/sedeibapi/externa/openapi.json"));
    }

    @Test
    void getOpenApiDocumentForOpenData_cachedCandidateStillWorks_skipsTheRaceEntirely() {
        String cached = "https://intranet.caib.es/sedeibapi/externa/openapi.json";
        DataApiClient client = buildClient(request -> {
            assertEquals(cached, request.url().toString());
            return Mono.just(okDocument("centrePerEtapa"));
        });

        OpenApiDocumentResult result = client.getOpenApiDocumentForOpenData("sedeib", false, cached);

        assertEquals(cached, result.getResolvedUrl());
        assertEquals("centrePerEtapa", result.getDocument().getPaths().get("/services/centre").getGet().getOperationId());
    }

    @Test
    void getOpenApiDocumentForOpenData_cachedCandidateStoppedWorking_fallsBackToRace() {
        String staleCache = "https://intranet.caib.es/sedeibapi/externa/api-docs.json";
        DataApiClient client = buildClient(request -> {
            if (request.url().toString().equals(staleCache)) {
                return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
            }
            if (request.url().toString().endsWith("openapi.json")) {
                return Mono.just(okDocument("centrePerEtapa"));
            }
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        });

        OpenApiDocumentResult result = client.getOpenApiDocumentForOpenData("sedeib", false, staleCache);

        assertEquals("https://intranet.caib.es/sedeibapi/externa/openapi.json", result.getResolvedUrl());
    }

    @Test
    void getOpenApiDocumentForOpenData_cachedCandidateHangs_timesOutFastAndFallsBackToRaceInsteadOfWaitingTheFullClientTimeout() {
        String hangingCache = "https://intranet.caib.es/sedeibapi/externa/api-docs.json";
        DataApiClient client = buildClient(request -> {
            if (request.url().toString().equals(hangingCache)) {
                return Mono.never();
            }
            if (request.url().toString().endsWith("openapi.json")) {
                return Mono.just(okDocument("centrePerEtapa"));
            }
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        });

        long start = System.currentTimeMillis();
        OpenApiDocumentResult result = client.getOpenApiDocumentForOpenData("sedeib", false, hangingCache);
        long elapsedMillis = System.currentTimeMillis() - start;

        assertEquals("https://intranet.caib.es/sedeibapi/externa/openapi.json", result.getResolvedUrl());
        assertTrue(elapsedMillis < 8000,
                "Expected the short cached-candidate timeout (3s) to cut in well before the WebClient's own 10s response timeout, took " + elapsedMillis + "ms");
    }

    @Test
    void getOpenApiDocumentForOpenData_useOpenDataUrlTrue_fetchesFromExplicitUrlInsteadOfRacing() {
        DataApiClient client = buildClient(request -> {
            assertEquals("https://example.test/custom/openapi.json", request.url().toString());
            return Mono.just(okDocument("customGet"));
        });

        OpenApiDocumentResult result = client.getOpenApiDocumentForOpenData("sedeib", true, "https://example.test/custom/openapi.json");

        assertEquals("https://example.test/custom/openapi.json", result.getResolvedUrl());
        assertEquals("customGet", result.getDocument().getPaths().get("/services/centre").getGet().getOperationId());
    }

    @Test
    void getOpenApiDocumentForOpenData_everyCandidateFails_throwsIntegrationUnavailableException() {
        DataApiClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build()));

        assertThrows(IntegrationUnavailableException.class,
                () -> client.getOpenApiDocumentForOpenData("unknown-app", false, null));
    }

    @Test
    void getOpenApiDocumentForOpenData_useOpenDataUrlTrueMalformedUrl_throwsIntegrationUnavailableException() {
        DataApiClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.OK).build()));

        assertThrows(IntegrationUnavailableException.class,
                () -> client.getOpenApiDocumentForOpenData("sedeib", true, "not a url"));
    }

    @Test
    void getOpenApiDocumentForOpenData_useOpenDataUrlTrueUnderlyingTransportTimeout_throwsIntegrationTimeoutException() {
        DataApiClient client = buildClient(request ->
                Mono.error(new java.util.concurrent.TimeoutException("simulated read timeout")));

        assertThrows(IntegrationTimeoutException.class,
                () -> client.getOpenApiDocumentForOpenData("sedeib", true, "https://example.test/custom/openapi.json"));
    }

    @Test
    void getOpenApiDocumentForReuse_noCache_racesEveryCandidateAndKeepsTheOneThatResponds() {
        DataApiClient client = buildClient(request -> {
            if (request.url().toString().endsWith("swagger.json")) {
                return Mono.just(okDocument("centrePerEtapa"));
            }
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        });

        OpenApiDocumentResult result = client.getOpenApiDocumentForReuse("sedeib", false, null);

        assertEquals("https://intranet.caib.es/sedeibapi/externa/swagger.json", result.getResolvedUrl());
        assertEquals("centrePerEtapa", result.getDocument().getPaths().get("/services/centre").getGet().getOperationId());
    }

    @Test
    void getOpenApiDocumentForReuse_useReuseUrlTrue_fetchesFromExplicitUrlInsteadOfRacing() {
        DataApiClient client = buildClient(request -> {
            assertEquals("https://example.test/custom/back", request.url().toString());
            return Mono.just(okDocument("customGet"));
        });

        OpenApiDocumentResult result = client.getOpenApiDocumentForReuse("sedeib", true, "https://example.test/custom/back");

        assertEquals("https://example.test/custom/back", result.getResolvedUrl());
        assertEquals("customGet", result.getDocument().getPaths().get("/services/centre").getGet().getOperationId());
    }

    @Test
    void getOpenApiDocumentForReuse_everyCandidateFails_throwsIntegrationUnavailableException() {
        DataApiClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build()));

        assertThrows(IntegrationUnavailableException.class,
                () -> client.getOpenApiDocumentForReuse("unknown-app", false, null));
    }
}
