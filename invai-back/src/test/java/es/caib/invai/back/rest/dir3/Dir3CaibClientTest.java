package es.caib.invai.back.rest.dir3;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Dir3CaibClient}. {@link WebClient} is wired with a stub
 * {@link ExchangeFunction} instead of a real {@code ClientHttpConnector}, so every test runs with
 * no real network access and verifies that the client builds the expected request against
 * {@code invai-api-interna} rather than calling DIR3CAIB directly.
 */
class Dir3CaibClientTest {

    private static final String EXPECTED_TREE_URI =
            "https://integracions.example.local/dir3/tree?rootCode=A04003003&coOfficial=true";

    private Dir3CaibClient buildClient(ExchangeFunction exchangeFunction) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://integracions.example.local")
                .exchangeFunction(exchangeFunction)
                .build();

        Dir3CaibClient client = new Dir3CaibClient();
        ReflectionTestUtils.setField(client, "integracionsWebClient", webClient);
        return client;
    }

    @Test
    void getTree_matchesFound_returnsMappedList() {
        Dir3CaibClient client = buildClient(request -> {
            assertEquals(EXPECTED_TREE_URI, request.url().toString());
            assertEquals(HttpMethod.GET, request.method());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("[{\"codigo\":\"A04003003\",\"denominacion\":\"Gobierno de las Illes Balears\","
                            + "\"denominacionCooficial\":\"Govern de les Illes Balears\",\"codUnidadSuperior\":\"A99999999\","
                            + "\"nivelJerarquico\":1}]")
                    .build());
        });

        List<UnidadRest> result = client.getTree("A04003003", true);

        assertEquals(1, result.size());
        assertEquals("A04003003", result.get(0).getCodigo());
        assertEquals("Govern de les Illes Balears", result.get(0).getDenominacionCooficial());
        assertEquals("A99999999", result.get(0).getCodUnidadSuperior());
        assertEquals(1, result.get(0).getNivelJerarquico());
    }

    @Test
    void getTree_noContent_returnsEmptyList() {
        Dir3CaibClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build()));

        List<UnidadRest> result = client.getTree("A04003003", true);

        assertTrue(result.isEmpty());
    }

    @Test
    void getTree_serverError_throwsBusinessRuleException() {
        Dir3CaibClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> client.getTree("A04003003", true));

        assertEquals(Constants.ERR_ADMUNIT_DIR3_UNAVAILABLE, ex.getMessage());
    }
}
