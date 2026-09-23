package es.caib.invai.api.interna.rest.soffid;

import es.caib.invai.api.interna.config.SoffidConfig;
import es.caib.invai.api.interna.exception.IntegrationUnavailableException;
import es.caib.invai.api.interna.rest.soffid.SoffidClient;
import es.caib.invai.api.interna.rest.soffid.SoffidRole;
import es.caib.invai.api.interna.rest.soffid.SoffidUser;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SoffidClient}, exercising it against a {@link WebClient} wired with a
 * custom {@code ExchangeFunction} lambda instead of a live HTTP connection - the Spring-native way
 * to test {@code WebClient} without chained-mock complexity.
 */
class SoffidClientTest {

    private SoffidClient client;

    @BeforeEach
    void setUp() {
        client = new SoffidClient();
        SoffidConfig soffidConfig = new SoffidConfig();
        ReflectionTestUtils.setField(soffidConfig, "searchPath", "/soffid/webservice/scim2/v1/User/");
        ReflectionTestUtils.setField(soffidConfig, "roleSearchPath", "/soffid/webservice/scim2/v1/Role/");
        ReflectionTestUtils.setField(client, "soffidConfig", soffidConfig);
    }

    private void wireWebClient(ExchangeFunction exchangeFunction) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://soffid.example.test")
                .exchangeFunction(exchangeFunction)
                .build();
        ReflectionTestUtils.setField(client, "soffidWebClient", webClient);
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
    void searchUsers_withFullName_returnsMappedUsersAndTotalCount() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(ONE_RESULT_BODY)
                .build()));

        Page<SoffidUser> result = client.searchUsers("Joan", PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals("Joan", result.getContent().get(0).getFirstName());
        assertEquals("Puig", result.getContent().get(0).getLastName());
        assertEquals("joan.puig@caib.es", result.getContent().get(0).getEmailAddress());
        assertTrue(result.getContent().get(0).isActive());
    }

    @Test
    void searchUsers_blankFullName_sendsFilterWithoutNameClauseAndCorrectPagination() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_RESULT_BODY)
                    .build());
        });

        client.searchUsers(null, PageRequest.of(1, 5));

        assertTrue(capturedUrl[0].contains("filter=active%20eq%20true")
                        || capturedUrl[0].contains("filter=active+eq+true"),
                "Expected an unfiltered 'active eq true' filter, got: " + capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("startIndex=6"), "Expected startIndex=6, got: " + capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("count=5"), "Expected count=5, got: " + capturedUrl[0]);
    }

    @Test
    void searchUsers_multiWordSearch_sendsOneOrClausePerWord() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_RESULT_BODY)
                    .build());
        });

        client.searchUsers("Joan Fuster", PageRequest.of(0, 20));

        String decodedUrl = URLDecoder.decode(capturedUrl[0], StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("(fullName co 'Joan' or userName co 'Joan')"), decodedUrl);
        assertTrue(decodedUrl.contains("(fullName co 'Fuster' or userName co 'Fuster')"), decodedUrl);
        assertTrue(decodedUrl.contains("active eq true"), decodedUrl);
    }

    @Test
    void searchUsers_emptyResources_returnsEmptyPage() {
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

        Page<SoffidUser> result = client.searchUsers("Nobody", PageRequest.of(0, 20));

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void searchUsers_searchTextMatchesOnlyUserName_stillResolvesInASingleRequest() {
        List<String> capturedUrls = new ArrayList<>();
        wireWebClient(request -> {
            capturedUrls.add(request.url().toString());
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_RESULT_BODY)
                    .build());
        });

        Page<SoffidUser> result = client.searchUsers("u00004", PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals("u00004", result.getContent().get(0).getUserName());
        assertEquals(1, capturedUrls.size(), "The OR filter resolves name-or-code matches in a single request");
        String decodedUrl = URLDecoder.decode(capturedUrls.get(0), StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("(fullName co 'u00004' or userName co 'u00004')"), decodedUrl);
        assertTrue(decodedUrl.contains("active eq true"), decodedUrl);
    }

    @Test
    void searchUsers_unauthorized_throwsIntegrationUnavailableException() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED).build()));

        assertThrows(IntegrationUnavailableException.class,
                () -> client.searchUsers("Joan", PageRequest.of(0, 20)));
    }

    @Test
    void searchUsers_connectionFailure_throwsIntegrationUnavailableException() {
        wireWebClient(request -> Mono.error(new WebClientRequestException(
                new IOException("Connection refused"),
                HttpMethod.GET,
                URI.create("https://soffid.example.test/soffid/webservice/scim2/v1/User/"),
                new HttpHeaders())));

        assertThrows(IntegrationUnavailableException.class,
                () -> client.searchUsers("Joan", PageRequest.of(0, 20)));
    }

    @Test
    void searchByEmail_matchFound_returnsFirstResult() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(ONE_RESULT_BODY)
                .build()));

        SoffidUser result = client.searchByEmail("joan.puig@caib.es");

        assertEquals("Joan", result.getFirstName());
        assertEquals("Puig", result.getLastName());
        assertEquals("joan.puig@caib.es", result.getEmailAddress());
    }

    @Test
    void searchByEmail_sendsExactEmailFilterWithoutPagingParams() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_RESULT_BODY)
                    .build());
        });

        client.searchByEmail("joan.puig@caib.es");

        String decodedUrl = URLDecoder.decode(capturedUrl[0], StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("emailAddress eq 'joan.puig@caib.es'"), decodedUrl);
        assertTrue(decodedUrl.contains("active eq true"), decodedUrl);
        assertTrue(!capturedUrl[0].contains("startIndex") && !capturedUrl[0].contains("count"), capturedUrl[0]);
    }

    @Test
    void searchByEmail_noMatch_returnsNull() {
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

        assertNull(client.searchByEmail("nobody@caib.es"));
    }

    @Test
    void searchByEmail_unauthorized_throwsIntegrationUnavailableException() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED).build()));

        assertThrows(IntegrationUnavailableException.class, () -> client.searchByEmail("joan.puig@caib.es"));
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

        Page<SoffidRole> result = client.searchRoles("AD", PageRequest.of(0, 20));

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

        client.searchRoles(null, PageRequest.of(1, 5));

        String decodedUrl = URLDecoder.decode(capturedUrl[0], StandardCharsets.UTF_8);
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

        client.searchRoles("role", PageRequest.of(0, 20));

        String decodedUrl = URLDecoder.decode(capturedUrl[0], StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("name sw 'INV_'"), decodedUrl);
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

        Page<SoffidRole> result = client.searchRoles("Nobody", PageRequest.of(0, 20));

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void searchRoles_unauthorized_throwsIntegrationUnavailableException() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED).build()));

        assertThrows(IntegrationUnavailableException.class,
                () -> client.searchRoles("AD", PageRequest.of(0, 20)));
    }

    @Test
    void searchRoles_connectionFailure_throwsIntegrationUnavailableException() {
        wireWebClient(request -> Mono.error(new WebClientRequestException(
                new IOException("Connection refused"),
                HttpMethod.GET,
                URI.create("https://soffid.example.test/soffid/webservice/scim2/v1/Role/"),
                new HttpHeaders())));

        assertThrows(IntegrationUnavailableException.class,
                () -> client.searchRoles("AD", PageRequest.of(0, 20)));
    }

}
