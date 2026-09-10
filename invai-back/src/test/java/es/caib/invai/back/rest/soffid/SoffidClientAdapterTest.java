package es.caib.invai.back.rest.soffid;

import es.caib.invai.back.config.SoffidConfig;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SoffidClientAdapter}, exercising it against a {@link WebClient} wired with
 * a custom {@code ExchangeFunction} lambda instead of a live HTTP connection - the Spring-native
 * way to test {@code WebClient} without chained-mock complexity ({@code MockRestServiceServer}
 * only works with {@code RestTemplate}/{@code RestClient}).
 */
class SoffidClientAdapterTest {

    private SoffidClientAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SoffidClientAdapter();
        SoffidConfig soffidConfig = new SoffidConfig();
        ReflectionTestUtils.setField(soffidConfig, "searchPath", "/soffid/webservice/scim2/v1/User/");
        ReflectionTestUtils.setField(soffidConfig, "roleSearchPath", "/soffid/webservice/scim2/v1/Role/");
        ReflectionTestUtils.setField(adapter, "soffidConfig", soffidConfig);
    }

    private void wireWebClient(ExchangeFunction exchangeFunction) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://soffid.example.test")
                .exchangeFunction(exchangeFunction)
                .build();
        ReflectionTestUtils.setField(adapter, "soffidWebClient", webClient);
    }

    private static final String ONE_RESULT_BODY = """
            {
              "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
              "totalResults": 1,
              "startIndex": 1,
              "itemsPerPage": 1,
              "Resources": [
                {
                  "id": 1603124,
                  "userName": "u00004",
                  "firstName": "Joan",
                  "lastName": "Puig",
                  "emailAddress": "joan.puig@caib.es",
                  "active": true
                }
              ]
            }
            """;

    @Test
    void search_withFullName_returnsMappedUsersAndTotalCount() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(ONE_RESULT_BODY)
                .build()));

        Page<SoffidUser> result = adapter.search("Joan", PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals("Joan", result.getContent().get(0).getFirstName());
        assertEquals("Puig", result.getContent().get(0).getLastName());
        assertEquals("joan.puig@caib.es", result.getContent().get(0).getEmailAddress());
        assertTrue(result.getContent().get(0).isActive());
    }

    @Test
    void search_blankFullName_sendsFilterWithoutNameClauseAndCorrectPagination() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_RESULT_BODY)
                    .build());
        });

        // page 1 (0-based), size 5 -> SCIM startIndex must be 6 (1-based).
        adapter.search(null, PageRequest.of(1, 5));

        assertTrue(capturedUrl[0].contains("filter=active%20eq%20true")
                || capturedUrl[0].contains("filter=active+eq+true"),
                "Expected an unfiltered 'active eq true' filter, got: " + capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("startIndex=6"), "Expected startIndex=6, got: " + capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("count=5"), "Expected count=5, got: " + capturedUrl[0]);
    }

    @Test
    void search_multiWordFullName_sendsOneClausePerWord() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_RESULT_BODY)
                    .build());
        });

        adapter.search("Joan Fuster", PageRequest.of(0, 20));

        String decodedUrl = java.net.URLDecoder.decode(capturedUrl[0], java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("fullName co 'Joan'"), decodedUrl);
        assertTrue(decodedUrl.contains("fullName co 'Fuster'"), decodedUrl);
        assertTrue(decodedUrl.contains("active eq true"), decodedUrl);
    }

    @Test
    void search_emptyResources_returnsEmptyPage() {
        String body = """
                {
                  "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
                  "totalResults": 0,
                  "startIndex": 1,
                  "itemsPerPage": 0,
                  "Resources": []
                }
                """;
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build()));

        Page<SoffidUser> result = adapter.search("Nobody", PageRequest.of(0, 20));

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void search_unauthorized_throwsBusinessRuleException() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> adapter.search("Joan", PageRequest.of(0, 20)));
        assertEquals(Constants.ERR_SOFFID_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void search_connectionFailure_throwsBusinessRuleException() {
        wireWebClient(request -> Mono.error(new WebClientRequestException(
                new IOException("Connection refused"),
                HttpMethod.GET,
                URI.create("https://soffid.example.test/soffid/webservice/scim2/v1/User/"),
                new HttpHeaders())));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> adapter.search("Joan", PageRequest.of(0, 20)));
        assertEquals(Constants.ERR_SOFFID_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void findByEmail_matchFound_returnsFirstResult() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(ONE_RESULT_BODY)
                .build()));

        SoffidUser result = adapter.findByEmail("joan.puig@caib.es");

        assertEquals("Joan", result.getFirstName());
        assertEquals("Puig", result.getLastName());
        assertEquals("joan.puig@caib.es", result.getEmailAddress());
    }

    @Test
    void findByEmail_sendsExactEmailFilterAndSingleResultPaging() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_RESULT_BODY)
                    .build());
        });

        adapter.findByEmail("joan.puig@caib.es");

        String decodedUrl = java.net.URLDecoder.decode(capturedUrl[0], java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("emailAddress eq 'joan.puig@caib.es'"), decodedUrl);
        assertTrue(decodedUrl.contains("active eq true"), decodedUrl);
        assertTrue(capturedUrl[0].contains("startIndex=1"), capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("count=1"), capturedUrl[0]);
    }

    @Test
    void findByEmail_noMatch_returnsNull() {
        String body = """
                {
                  "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
                  "totalResults": 0,
                  "startIndex": 1,
                  "itemsPerPage": 0,
                  "Resources": []
                }
                """;
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build()));

        assertNull(adapter.findByEmail("nobody@caib.es"));
    }

    @Test
    void findByEmail_unauthorized_throwsBusinessRuleException() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> adapter.findByEmail("joan.puig@caib.es"));
        assertEquals(Constants.ERR_SOFFID_UNAVAILABLE, ex.getMessage());
    }

    private static final String ONE_ROLE_RESULT_BODY = """
            {
              "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
              "totalResults": 1,
              "startIndex": 1,
              "Resources": [
                {
                  "schemas": ["urn:soffid:com.soffid.iam.api.Role"],
                  "id": 393195,
                  "name": "AD role",
                  "description": "AD role admin",
                  "system": "ad",
                  "informationSystemName": "Operation/Business process/ad"
                }
              ]
            }
            """;

    @Test
    void searchRoles_withName_returnsMappedRolesAndTotalCount() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(ONE_ROLE_RESULT_BODY)
                .build()));

        Page<SoffidRole> result = adapter.searchRoles("AD", PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals(393195L, result.getContent().get(0).getId());
        assertEquals("AD role", result.getContent().get(0).getName());
        assertEquals("AD role admin", result.getContent().get(0).getDescription());
    }

    @Test
    void searchRoles_blankName_sendsOnlyBaseInvNamespaceFilterAndCorrectPagination() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_ROLE_RESULT_BODY)
                    .build());
        });

        // page 1 (0-based), size 5 -> SCIM startIndex must be 6 (1-based).
        adapter.searchRoles(null, PageRequest.of(1, 5));

        String decodedUrl = java.net.URLDecoder.decode(capturedUrl[0], java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("filter=name sw 'INV_'"), "Expected only the base INV_ namespace filter, got: " + decodedUrl);
        assertTrue(capturedUrl[0].contains("startIndex=6"), "Expected startIndex=6, got: " + capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("count=5"), "Expected count=5, got: " + capturedUrl[0]);
    }

    @Test
    void searchRoles_multiWordName_sendsBaseInvNamespaceFilterAndOneClausePerWord() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_ROLE_RESULT_BODY)
                    .build());
        });

        adapter.searchRoles("AD role", PageRequest.of(0, 20));

        String decodedUrl = java.net.URLDecoder.decode(capturedUrl[0], java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("name sw 'INV_'"), decodedUrl);
        assertTrue(decodedUrl.contains("name co 'AD'"), decodedUrl);
        assertTrue(decodedUrl.contains("name co 'role'"), decodedUrl);
    }

    @Test
    void searchRoles_emptyResources_returnsEmptyPage() {
        String body = """
                {
                  "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
                  "totalResults": 0,
                  "startIndex": 1,
                  "Resources": []
                }
                """;
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build()));

        Page<SoffidRole> result = adapter.searchRoles("Nobody", PageRequest.of(0, 20));

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void searchRoles_unauthorized_throwsBusinessRuleException() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED).build()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> adapter.searchRoles("AD", PageRequest.of(0, 20)));
        assertEquals(Constants.ERR_SOFFID_UNAVAILABLE, ex.getMessage());
    }

    @Test
    void searchRoles_connectionFailure_throwsBusinessRuleException() {
        wireWebClient(request -> Mono.error(new WebClientRequestException(
                new IOException("Connection refused"),
                HttpMethod.GET,
                URI.create("https://soffid.example.test/soffid/webservice/scim2/v1/Role/"),
                new HttpHeaders())));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> adapter.searchRoles("AD", PageRequest.of(0, 20)));
        assertEquals(Constants.ERR_SOFFID_UNAVAILABLE, ex.getMessage());
    }
}
