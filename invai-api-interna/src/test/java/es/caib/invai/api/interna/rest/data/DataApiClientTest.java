package es.caib.invai.api.interna.rest.data;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DataApiClient}. {@link WebClient} is wired with a stub
 * {@link ExchangeFunction} instead of a real {@code ClientHttpConnector}, so every test runs with
 * no real network access and no chained-mock setup of WebClient's fluent builder types.
 */
class DataApiClientTest {

    private static final String URL = "https://intranet.caib.es/sedeibapi/externa";

    private final List<String> requested = new ArrayList<>();

    private DataApiClient buildClient(ExchangeFunction exchangeFunction) {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> {
                    requested.add(request.url().toString());
                    return exchangeFunction.exchange(request);
                })
                .build();

        DataApiClient client = new DataApiClient();
        ReflectionTestUtils.setField(client, "dataWebClient", webClient);
        return client;
    }

    private DataApiClient clientAnswering(HttpStatus status) {
        return buildClient(request -> Mono.just(ClientResponse.create(status).build()));
    }

    @Test
    void checkUrl_2xx_isValid_andQueriesTheGivenUrl() {
        UrlCheckResult result = clientAnswering(HttpStatus.OK).checkUrl(URL);

        assertTrue(result.isValid());
        assertEquals(200, result.getStatusCode());
        assertEquals(URL, result.getUrl());
        assertEquals(List.of(URL), requested);
    }

    @Test
    void checkUrl_anyOtherSuccessCode_isValid() {
        UrlCheckResult result = clientAnswering(HttpStatus.NO_CONTENT).checkUrl(URL);

        assertTrue(result.isValid());
        assertEquals(204, result.getStatusCode());
    }

    @Test
    void checkUrl_4xx_isNotValid_andReportsTheStatusCode() {
        UrlCheckResult result = clientAnswering(HttpStatus.NOT_FOUND).checkUrl(URL);

        assertFalse(result.isValid());
        assertEquals(404, result.getStatusCode());
    }

    @Test
    void checkUrl_5xx_isNotValid_andReportsTheStatusCode() {
        UrlCheckResult result = clientAnswering(HttpStatus.INTERNAL_SERVER_ERROR).checkUrl(URL);

        assertFalse(result.isValid());
        assertEquals(500, result.getStatusCode());
    }

    @Test
    void checkUrl_anUnfollowedRedirect_isNotValid() {
        UrlCheckResult result = clientAnswering(HttpStatus.FOUND).checkUrl(URL);

        assertFalse(result.isValid());
        assertEquals(302, result.getStatusCode());
    }

    @Test
    void checkUrl_networkFailure_isNotValid_withoutStatusCode() {
        DataApiClient client = buildClient(request -> Mono.error(new IllegalStateException("connection refused")));

        UrlCheckResult result = client.checkUrl(URL);

        assertFalse(result.isValid());
        assertNull(result.getStatusCode());
        assertEquals(URL, result.getUrl());
    }

    @Test
    void checkUrl_timeout_isNotValid_withoutStatusCode() {
        DataApiClient client = buildClient(request -> Mono.error(new java.util.concurrent.TimeoutException("read timed out")));

        UrlCheckResult result = client.checkUrl(URL);

        assertFalse(result.isValid());
        assertNull(result.getStatusCode());
    }

    @Test
    void checkUrl_aTypedUrlThatIsNotAnAbsoluteHttpUrl_isNeverQueried() {
        DataApiClient client = clientAnswering(HttpStatus.OK);

        for (String url : new String[]{"", "sedeibapi/externa", "file:///etc/passwd", "ftp://intranet.caib.es/x", "https://", "not a url"}) {
            UrlCheckResult result = client.checkUrl(url);

            assertFalse(result.isValid(), url);
            assertNull(result.getStatusCode(), url);
        }
        assertTrue(requested.isEmpty());
    }

    @Test
    void checkUrl_httpIsAccepted() {
        UrlCheckResult result = clientAnswering(HttpStatus.OK).checkUrl("http://intranet.caib.es/sedeibapi/interna");

        assertTrue(result.isValid());
    }
}
