package es.caib.invai.back.rest.data;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.DataTimeoutException;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DataApiClient}. {@link WebClient} is wired with a stub
 * {@link ExchangeFunction} instead of a real {@code ClientHttpConnector}, so every test runs with
 * no real network access and verifies that the client builds the expected request against
 * {@code invai-api-interna} rather than querying the URL directly.
 */
class DataApiClientTest {

    private static final String URL = "https://intranet.caib.es/sedeibapi/externa";

    private DataApiClient buildClient(ExchangeFunction exchangeFunction) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://integracions.example.local")
                .exchangeFunction(exchangeFunction)
                .build();

        DataApiClient client = new DataApiClient();
        ReflectionTestUtils.setField(client, "integracionsWebClient", webClient);
        return client;
    }

    private static Mono<ClientResponse> json(String body) {
        return Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build());
    }

    @Test
    void checkUrl_callsTheCheckEndpointWithTheUrlAndReturnsItsResult() {
        DataApiClient client = buildClient(request -> {
            assertEquals(HttpMethod.GET, request.method());
            assertEquals("/data/check", request.url().getPath());
            assertEquals("url=" + URL, request.url().getQuery());
            return json("{\"url\":\"" + URL + "\",\"valid\":true,\"statusCode\":200}");
        });

        UrlCheckResult result = client.checkUrl(URL);

        assertTrue(result.isValid());
        assertEquals(200, result.getStatusCode());
        assertEquals(URL, result.getUrl());
    }

    @Test
    void checkUrl_reportsAUrlThatDoesNotWork() {
        DataApiClient client = buildClient(request -> json("{\"url\":\"" + URL + "\",\"valid\":false,\"statusCode\":404}"));

        UrlCheckResult result = client.checkUrl(URL);

        assertFalse(result.isValid());
        assertEquals(404, result.getStatusCode());
    }

    @Test
    void checkUrl_aResultWithNoStatusCode_keepsItNull() {
        DataApiClient client = buildClient(request -> json("{\"url\":\"" + URL + "\",\"valid\":false,\"statusCode\":null}"));

        UrlCheckResult result = client.checkUrl(URL);

        assertFalse(result.isValid());
        assertNull(result.getStatusCode());
    }

    @Test
    void checkUrl_encodesAUrlThatHasItsOwnQueryString() {
        String url = "https://intranet.caib.es/sedeibapi/externa?lang=ca&format=json";
        DataApiClient client = buildClient(request -> {
            assertEquals("url=" + url, request.url().getQuery());
            assertTrue(request.url().getRawQuery().contains("%26format"), request.url().getRawQuery());
            return json("{\"url\":\"x\",\"valid\":true,\"statusCode\":200}");
        });

        assertTrue(client.checkUrl(url).isValid());
    }

    @Test
    void checkUrl_gatewayTimeout_throwsDataTimeoutException() {
        DataApiClient client = buildClient(request -> Mono.just(ClientResponse.create(HttpStatus.GATEWAY_TIMEOUT).build()));

        assertThrows(DataTimeoutException.class, () -> client.checkUrl(URL));
    }

    @Test
    void checkUrl_otherErrorStatus_throwsUrlUnavailable() {
        DataApiClient client = buildClient(request -> Mono.just(ClientResponse.create(HttpStatus.BAD_GATEWAY).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> client.checkUrl(URL));

        assertEquals(Constants.ERR_DATA_URL_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void checkUrl_networkFailure_throwsUrlUnavailable() {
        DataApiClient client = buildClient(request -> Mono.error(new WebClientRequestException(
                new java.net.ConnectException("refused"), HttpMethod.GET, URI.create("https://integracions.example.local/data/check"), new HttpHeaders())));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> client.checkUrl(URL));

        assertEquals(Constants.ERR_DATA_URL_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void checkUrl_emptyBody_throwsUrlUnavailable() {
        DataApiClient client = buildClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> client.checkUrl(URL));

        assertEquals(Constants.ERR_DATA_URL_UNAVAILABLE, ex.getMessage());
    }
}
