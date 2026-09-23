package es.caib.invai.back.rest.soffid;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SoffidClientAdapter}. {@link WebClient} is wired with a stub
 * {@link ExchangeFunction} instead of a real {@code ClientHttpConnector}, so every test runs with
 * no real network access and verifies that the adapter builds the expected request against
 * {@code invai-api-interna} rather than calling Soffid directly.
 */
class SoffidClientAdapterTest {

    private SoffidClientAdapter buildAdapter(ExchangeFunction exchangeFunction) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://integracions.example.local")
                .exchangeFunction(exchangeFunction)
                .build();

        SoffidClientAdapter adapter = new SoffidClientAdapter();
        ReflectionTestUtils.setField(adapter, "integracionsWebClient", webClient);
        return adapter;
    }

    private static final String ONE_USER_BODY = """
            {
              "content": [
                {
                  "userName": "u00004",
                  "firstName": "Joan",
                  "lastName": "Puig",
                  "emailAddress": "joan.puig@caib.es",
                  "active": true
                }
              ],
              "totalElements": 1
            }
            """;

    @Test
    void search_withFullName_returnsMappedUsersAndTotalCount() {
        SoffidClientAdapter adapter = buildAdapter(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(ONE_USER_BODY)
                .build()));

        Page<SoffidUser> result = adapter.search("Joan", PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals("Joan", result.getContent().get(0).getFirstName());
        assertEquals("Puig", result.getContent().get(0).getLastName());
        assertEquals("joan.puig@caib.es", result.getContent().get(0).getEmailAddress());
        assertTrue(result.getContent().get(0).isActive());
    }

    @Test
    void search_blankFullName_sendsEmptySearchParamAndCorrectPagination() {
        String[] capturedUrl = new String[1];
        SoffidClientAdapter adapter = buildAdapter(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_USER_BODY)
                    .build());
        });

        adapter.search(null, PageRequest.of(1, 5));

        assertTrue(capturedUrl[0].contains("/soffid/users"), capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("search="), "Expected an empty search param, got: " + capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("page=1"), capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("size=5"), capturedUrl[0]);
    }

    @Test
    void search_multiWordFullName_forwardsRawSearchText() {
        String[] capturedUrl = new String[1];
        SoffidClientAdapter adapter = buildAdapter(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_USER_BODY)
                    .build());
        });

        adapter.search("Joan Fuster", PageRequest.of(0, 20));

        String decodedUrl = URLDecoder.decode(capturedUrl[0], StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("search=Joan Fuster"), decodedUrl);
    }

    @Test
    void search_emptyContent_returnsEmptyPage() {
        String body = """
                {
                  "content": [],
                  "totalElements": 0
                }
                """;
        SoffidClientAdapter adapter = buildAdapter(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build()));

        Page<SoffidUser> result = adapter.search("Nobody", PageRequest.of(0, 20));

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void search_serverError_throwsBusinessRuleException() {
        SoffidClientAdapter adapter = buildAdapter(request ->
                Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> adapter.search("Joan", PageRequest.of(0, 20)));
        assertEquals(Constants.ERR_SOFFID_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void search_connectionFailure_throwsBusinessRuleException() {
        SoffidClientAdapter adapter = buildAdapter(request -> Mono.error(new WebClientRequestException(
                new IOException("Connection refused"),
                HttpMethod.GET,
                URI.create("https://integracions.example.local/soffid/users"),
                new HttpHeaders())));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> adapter.search("Joan", PageRequest.of(0, 20)));
        assertEquals(Constants.ERR_SOFFID_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void findByEmail_matchFound_returnsUser() {
        String body = """
                {
                  "userName": "u00004",
                  "firstName": "Joan",
                  "lastName": "Puig",
                  "emailAddress": "joan.puig@caib.es",
                  "active": true
                }
                """;
        SoffidClientAdapter adapter = buildAdapter(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build()));

        SoffidUser result = adapter.findByEmail("joan.puig@caib.es");

        assertEquals("Joan", result.getFirstName());
        assertEquals("Puig", result.getLastName());
        assertEquals("joan.puig@caib.es", result.getEmailAddress());
    }

    @Test
    void findByEmail_sendsExactEmailParam() {
        String[] capturedUrl = new String[1];
        SoffidClientAdapter adapter = buildAdapter(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build());
        });

        adapter.findByEmail("joan.puig@caib.es");

        String decodedUrl = URLDecoder.decode(capturedUrl[0], StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("/soffid/users/by-email"), decodedUrl);
        assertTrue(decodedUrl.contains("email=joan.puig@caib.es"), decodedUrl);
    }

    @Test
    void findByEmail_noMatch_returnsNull() {
        SoffidClientAdapter adapter = buildAdapter(request ->
                Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build()));

        assertNull(adapter.findByEmail("nobody@caib.es"));
    }

    @Test
    void findByEmail_serverError_throwsBusinessRuleException() {
        SoffidClientAdapter adapter = buildAdapter(request ->
                Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> adapter.findByEmail("joan.puig@caib.es"));
        assertEquals(Constants.ERR_SOFFID_UNAVAILABLE, ex.getMessage());
    }

    private static final String ONE_ROLE_BODY = """
            {
              "content": [
                {
                  "id": 393195,
                  "name": "AD role",
                  "description": "AD role admin"
                }
              ],
              "totalElements": 1
            }
            """;

    @Test
    void searchRoles_withName_returnsMappedRolesAndTotalCount() {
        SoffidClientAdapter adapter = buildAdapter(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(ONE_ROLE_BODY)
                .build()));

        Page<SoffidRole> result = adapter.searchRoles("AD", PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals(393195L, result.getContent().get(0).getId());
        assertEquals("AD role", result.getContent().get(0).getName());
        assertEquals("AD role admin", result.getContent().get(0).getDescription());
    }

    @Test
    void searchRoles_blankName_sendsEmptyNameParamAndCorrectPagination() {
        String[] capturedUrl = new String[1];
        SoffidClientAdapter adapter = buildAdapter(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_ROLE_BODY)
                    .build());
        });

        adapter.searchRoles(null, PageRequest.of(1, 5));

        assertTrue(capturedUrl[0].contains("/soffid/roles"), capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("page=1"), capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("size=5"), capturedUrl[0]);
    }

    @Test
    void searchRoles_emptyContent_returnsEmptyPage() {
        String body = """
                {
                  "content": [],
                  "totalElements": 0
                }
                """;
        SoffidClientAdapter adapter = buildAdapter(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build()));

        Page<SoffidRole> result = adapter.searchRoles("Nobody", PageRequest.of(0, 20));

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void searchRoles_serverError_throwsBusinessRuleException() {
        SoffidClientAdapter adapter = buildAdapter(request ->
                Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> adapter.searchRoles("AD", PageRequest.of(0, 20)));
        assertEquals(Constants.ERR_SOFFID_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void searchRoles_connectionFailure_throwsBusinessRuleException() {
        SoffidClientAdapter adapter = buildAdapter(request -> Mono.error(new WebClientRequestException(
                new IOException("Connection refused"),
                HttpMethod.GET,
                URI.create("https://integracions.example.local/soffid/roles"),
                new HttpHeaders())));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> adapter.searchRoles("AD", PageRequest.of(0, 20)));
        assertEquals(Constants.ERR_SOFFID_UNAVAILABLE, ex.getMessage());
    }
}
