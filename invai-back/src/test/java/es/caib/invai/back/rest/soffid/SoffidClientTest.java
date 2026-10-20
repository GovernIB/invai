package es.caib.invai.back.rest.soffid;

import es.caib.invai.back.exception.SoffidClientException;
import es.caib.invai.back.exception.SoffidTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SoffidClient}. {@link WebClient} is wired with a stub
 * {@link ExchangeFunction} instead of a real {@code ClientHttpConnector}, so every test runs with
 * no real network access and verifies that the client builds the expected request against
 * {@code invai-api-interna} rather than calling Soffid directly.
 */
class SoffidClientTest {

    private SoffidClient buildClient(ExchangeFunction exchangeFunction) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://integracions.example.local")
                .exchangeFunction(exchangeFunction)
                .build();

        SoffidClient client = new SoffidClient();
        ReflectionTestUtils.setField(client, "integracionsWebClient", webClient);
        return client;
    }

    @Test
    void searchUsers_success_returnsMappedPage() {
        SoffidClient client = buildClient(request -> {
            assertEquals("https://integracions.example.local/soffid/users?search=Joan&page=0&size=20",
                    request.url().toString());
            assertEquals(HttpMethod.GET, request.method());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"content\":[{\"userName\":\"u00004\",\"firstName\":\"Joan\"}],\"totalElements\":1}")
                    .build());
        });

        Page<SoffidUser> result = client.searchUsers("Joan", PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals("u00004", result.getContent().get(0).getUserName());
    }

    @Test
    void searchUsers_blankSearch_sendsEmptySearchParam() {
        SoffidClient client = buildClient(request -> {
            assertEquals("https://integracions.example.local/soffid/users?search=&page=1&size=5",
                    request.url().toString());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"content\":[],\"totalElements\":0}")
                    .build());
        });

        Page<SoffidUser> result = client.searchUsers(null, PageRequest.of(1, 5));

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void searchByEmail_matchFound_returnsUser() {
        SoffidClient client = buildClient(request -> {
            assertEquals("https://integracions.example.local/soffid/users/by-email?email=joan.puig@caib.es",
                    request.url().toString());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"emailAddress\":\"joan.puig@caib.es\"}")
                    .build());
        });

        SoffidUser result = client.searchByEmail("joan.puig@caib.es");

        assertEquals("joan.puig@caib.es", result.getEmailAddress());
    }

    @Test
    void searchByEmail_noContent_returnsNull() {
        SoffidClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build()));

        assertNull(client.searchByEmail("nobody@caib.es"));
    }

    @Test
    void searchRoles_success_returnsMappedPage() {
        SoffidClient client = buildClient(request -> {
            assertEquals("https://integracions.example.local/soffid/roles?name=AD&page=0&size=20",
                    request.url().toString());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"content\":[{\"name\":\"AD role\"}],\"totalElements\":1}")
                    .build());
        });

        Page<SoffidRole> result = client.searchRoles("AD", PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals("AD role", result.getContent().get(0).getName());
    }

    @Test
    void getRolesByIds_success_returnsMappedList() {
        SoffidClient client = buildClient(request -> {
            assertTrue(request.url().toString().startsWith("https://integracions.example.local/soffid/roles/by-ids?"));
            assertEquals(HttpMethod.GET, request.method());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("[{\"id\":100,\"name\":\"AD role\"}]")
                    .build());
        });

        List<SoffidRole> result = client.getRolesByIds(List.of(100L));

        assertEquals(1, result.size());
        assertEquals("AD role", result.get(0).getName());
    }

    @Test
    void getRolesByIds_gatewayTimeout_throwsSoffidTimeoutException() {
        SoffidClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.GATEWAY_TIMEOUT).build()));

        assertThrows(SoffidTimeoutException.class, () -> client.getRolesByIds(List.of(100L)));
    }

    @Test
    void getGrantedRoles_success_returnsMappedList() {
        SoffidClient client = buildClient(request -> {
            assertEquals("https://integracions.example.local/soffid/users/%24kadadafas/roles",
                    request.url().toString());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("[{\"id\":3492930,\"name\":\"INV_SUPER\"}]")
                    .build());
        });

        List<SoffidRole> result = client.getGrantedRoles("$kadadafas");

        assertEquals(1, result.size());
        assertEquals(3492930L, result.get(0).getId());
        assertEquals("INV_SUPER", result.get(0).getName());
    }

    @Test
    void getGrantedRoles_gatewayTimeout_throwsSoffidTimeoutException() {
        SoffidClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.GATEWAY_TIMEOUT).build()));

        assertThrows(SoffidTimeoutException.class, () -> client.getGrantedRoles("$kadadafas"));
    }

    @Test
    void resolveGroupDir3_found_returnsDir3Code() {
        SoffidClient client = buildClient(request -> {
            assertEquals("https://integracions.example.local/soffid/groups/sgaip/dir3",
                    request.url().toString());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"dir3Code\":\"A04027054\"}")
                    .build());
        });

        assertEquals("A04027054", client.resolveGroupDir3("sgaip"));
    }

    @Test
    void resolveGroupDir3_noContent_returnsNull() {
        SoffidClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build()));

        assertNull(client.resolveGroupDir3("nonexistent"));
    }

    @Test
    void searchUsers_gatewayTimeout_throwsSoffidTimeoutException() {
        SoffidClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.GATEWAY_TIMEOUT).build()));

        assertThrows(SoffidTimeoutException.class, () -> client.searchUsers("Joan", PageRequest.of(0, 20)));
    }

    @Test
    void searchUsers_badGateway_throwsSoffidClientExceptionWithStatusCode() {
        SoffidClient client = buildClient(request ->
                Mono.just(ClientResponse.create(HttpStatus.BAD_GATEWAY).build()));

        SoffidClientException ex = assertThrows(SoffidClientException.class,
                () -> client.searchUsers("Joan", PageRequest.of(0, 20)));
        assertEquals("502", ex.getErrorCode());
    }

    @Test
    void searchUsers_connectionFailure_throwsSoffidClientException() {
        SoffidClient client = buildClient(request -> Mono.error(new WebClientRequestException(
                new IOException("Connection refused"),
                HttpMethod.GET,
                URI.create("https://integracions.example.local/soffid/users"),
                new HttpHeaders())));

        assertThrows(SoffidClientException.class, () -> client.searchUsers("Joan", PageRequest.of(0, 20)));
    }
}
