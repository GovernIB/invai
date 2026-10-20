package es.caib.invai.back.rest.data;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.DataTimeoutException;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link DataApiClient}. {@link WebClient} is wired with a stub
 * {@link ExchangeFunction} instead of a real {@code ClientHttpConnector}, so every test runs with
 * no real network access and verifies that the client builds the expected request against
 * {@code invai-api-interna} rather than fetching the document directly.
 */
class DataApiClientTest {

    private DataApiClient buildClient(ExchangeFunction exchangeFunction) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://integracions.example.local")
                .exchangeFunction(exchangeFunction)
                .build();

        DataApiClient client = new DataApiClient();
        ReflectionTestUtils.setField(client, "integracionsWebClient", webClient);
        return client;
    }

    @Test
    void fetchOpenApiDocument_applicationName_returnsParsedDocumentAndResolvedUrl() {
        DataApiClient client = buildClient(request -> {
            assertEquals("https://integracions.example.local/opendata/document?applicationName=sedeib&useOpenDataUrl=false",
                    request.url().toString());
            assertEquals(HttpMethod.GET, request.method());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"document\":{\"paths\":{\"/services/centre\":{\"get\":{\"operationId\":\"centrePerEtapa\"}}}},"
                            + "\"resolvedUrl\":\"https://intranet.caib.es/sedeibapi/externa/openapi.json\"}")
                    .build());
        });

        OpenApiDocumentResult result = client.fetchOpenApiDocument("sedeib", false, null);

        assertEquals("centrePerEtapa", result.getDocument().getPaths().get("/services/centre").getGet().getOperationId());
        assertEquals("https://intranet.caib.es/sedeibapi/externa/openapi.json", result.getResolvedUrl());
    }

    @Test
    void fetchOpenApiDocument_useOpenDataUrlTrue_forwardsExplicitUrl() {
        DataApiClient client = buildClient(request -> {
            assertEquals("https://integracions.example.local/opendata/document?applicationName=sedeib"
                            + "&useOpenDataUrl=true&openDataUrl=https://example.test/custom/openapi.json",
                    request.url().toString());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"document\":{\"paths\":{\"/custom\":{\"get\":{\"operationId\":\"customGet\"}}}},"
                            + "\"resolvedUrl\":\"https://example.test/custom/openapi.json\"}")
                    .build());
        });

        OpenApiDocumentResult result = client.fetchOpenApiDocument("sedeib", true, "https://example.test/custom/openapi.json");

        assertEquals("customGet", result.getDocument().getPaths().get("/custom").getGet().getOperationId());
        assertEquals("https://example.test/custom/openapi.json", result.getResolvedUrl());
    }

    @Test
    void fetchOpenApiDocument_serverError_throwsBusinessRuleException() {
        DataApiClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> client.fetchOpenApiDocument("sedeib", false, null));

        assertEquals(Constants.ERR_OPENDATA_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void fetchOpenApiDocument_gatewayTimeout_throwsDataTimeoutException() {
        DataApiClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.GATEWAY_TIMEOUT).build()));

        assertThrows(DataTimeoutException.class, () -> client.fetchOpenApiDocument("sedeib", false, null));
    }

    @Test
    void fetchOpenApiDocument_notFound_throwsBusinessRuleException() {
        DataApiClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> client.fetchOpenApiDocument("unknown-app", false, null));

        assertEquals(Constants.ERR_OPENDATA_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void fetchReuseDocument_applicationName_returnsParsedDocumentAndResolvedUrl() {
        DataApiClient client = buildClient(request -> {
            assertEquals("https://integracions.example.local/reuse/document?applicationName=sedeib&useReuseUrl=false",
                    request.url().toString());
            assertEquals(HttpMethod.GET, request.method());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"document\":{\"paths\":{\"/services/centre\":{\"get\":{\"operationId\":\"centrePerEtapa\"}}}},"
                            + "\"resolvedUrl\":\"https://intranet.caib.es/sedeibapi/externa/swagger.json\"}")
                    .build());
        });

        OpenApiDocumentResult result = client.fetchReuseDocument("sedeib", false, null);

        assertEquals("centrePerEtapa", result.getDocument().getPaths().get("/services/centre").getGet().getOperationId());
        assertEquals("https://intranet.caib.es/sedeibapi/externa/swagger.json", result.getResolvedUrl());
    }

    @Test
    void fetchReuseDocument_useReuseUrlTrue_forwardsExplicitUrl() {
        DataApiClient client = buildClient(request -> {
            assertEquals("https://integracions.example.local/reuse/document?applicationName=sedeib"
                            + "&useReuseUrl=true&reuseUrl=https://example.test/custom/back",
                    request.url().toString());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"document\":{\"paths\":{\"/custom\":{\"get\":{\"operationId\":\"customGet\"}}}},"
                            + "\"resolvedUrl\":\"https://example.test/custom/back\"}")
                    .build());
        });

        OpenApiDocumentResult result = client.fetchReuseDocument("sedeib", true, "https://example.test/custom/back");

        assertEquals("customGet", result.getDocument().getPaths().get("/custom").getGet().getOperationId());
        assertEquals("https://example.test/custom/back", result.getResolvedUrl());
    }

    @Test
    void fetchReuseDocument_serverError_throwsBusinessRuleException() {
        DataApiClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> client.fetchReuseDocument("sedeib", false, null));

        assertEquals(Constants.ERR_REUSE_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void fetchReuseDocument_gatewayTimeout_throwsDataTimeoutException() {
        DataApiClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.GATEWAY_TIMEOUT).build()));

        assertThrows(DataTimeoutException.class, () -> client.fetchReuseDocument("sedeib", false, null));
    }

    @Test
    void fetchReuseDocument_notFound_throwsBusinessRuleException() {
        DataApiClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> client.fetchReuseDocument("unknown-app", false, null));

        assertEquals(Constants.ERR_REUSE_UNAVAILABLE, ex.getMessage());
    }
}
