package es.caib.invai.api.interna.rest.soffid;

import es.caib.invai.api.interna.config.SoffidConfig;
import es.caib.invai.api.interna.exception.IntegrationUnavailableException;
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
        ReflectionTestUtils.setField(soffidConfig, "groupSearchPath", "/soffid/webservice/scim2/v1/Group/");
        ReflectionTestUtils.setField(soffidConfig, "grantedRolePath", "/soffid/webservice/scim2/v1/RoleAccount");
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
        assertEquals(1603124L, result.getContent().get(0).getId());
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
        assertTrue(decodedUrl.contains("(fullName co 'Joan' or userName co 'Joan' or emailAddress co 'Joan')"), decodedUrl);
        assertTrue(decodedUrl.contains("(fullName co 'Fuster' or userName co 'Fuster' or emailAddress co 'Fuster')"), decodedUrl);
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
        assertTrue(decodedUrl.contains("(fullName co 'u00004' or userName co 'u00004' or emailAddress co 'u00004')"), decodedUrl);
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
    void searchRoles_blankName_sendsNoFilterParamAtAllWithCorrectPagination() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_ROLE_RESULT_BODY)
                    .build());
        });

        client.searchRoles(null, PageRequest.of(1, 5));

        assertTrue(!capturedUrl[0].contains("filter="), "Expected no filter param at all, got: " + capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("startIndex=6"), "Expected startIndex=6, got: " + capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("count=5"), "Expected count=5, got: " + capturedUrl[0]);
    }

    @Test
    void searchRoles_multiWordName_sendsOneContainsClausePerWordAndNoNamespaceClause() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_ROLE_RESULT_BODY)
                    .build());
        });

        client.searchRoles("INV_ role", PageRequest.of(0, 20));

        String decodedUrl = URLDecoder.decode(capturedUrl[0], StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("name co 'INV_'"), decodedUrl);
        assertTrue(decodedUrl.contains("name co 'role'"), decodedUrl);
        assertTrue(!decodedUrl.contains("sw"), "Expected no 'starts with' clause, got: " + decodedUrl);
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

    @Test
    void getRolesByIds_returnsMappedRoles() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(ONE_ROLE_RESULT_BODY)
                .build()));

        List<SoffidRole> result = client.getRolesByIds(List.of(393195L));

        assertEquals(1, result.size());
        assertEquals(393195L, result.get(0).getId());
        assertEquals("AD role", result.get(0).getName());
    }

    @Test
    void getRolesByIds_sendsOrClausePerIdAndCountMatchingRequestSize() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_ROLE_RESULT_BODY)
                    .build());
        });

        client.getRolesByIds(List.of(100L, 200L, 300L));

        String decodedUrl = URLDecoder.decode(capturedUrl[0], StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("id eq 100 or id eq 200 or id eq 300"), decodedUrl);
        assertTrue(capturedUrl[0].contains("count=3"), capturedUrl[0]);
    }

    @Test
    void getRolesByIds_someIdsNotFound_returnsFewerThanRequested() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(ONE_ROLE_RESULT_BODY)
                .build()));

        List<SoffidRole> result = client.getRolesByIds(List.of(393195L, 999999L));

        assertEquals(1, result.size());
    }

    @Test
    void getRolesByIds_unauthorized_throwsIntegrationUnavailableException() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED).build()));

        assertThrows(IntegrationUnavailableException.class, () -> client.getRolesByIds(List.of(393195L)));
    }

    private static final String ONE_ROLE_ACCOUNT_BODY = """
            {
              "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
              "totalResults": 1,
              "Resources": [
                {
                  "accountName": "$kadadafas",
                  "roleId": 3492930,
                  "roleName": "INV_SUPER",
                  "system": "soffid",
                  "roleDescription": "INV_SUPER per a SOFFID"
                }
              ]
            }
            """;

    @Test
    void getUserRoles_returnsMappedRoles() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(ONE_ROLE_ACCOUNT_BODY)
                .build()));

        List<SoffidRole> result = client.getUserRoles("$kadadafas");

        assertEquals(1, result.size());
        assertEquals(3492930L, result.get(0).getId());
        assertEquals("INV_SUPER", result.get(0).getName());
        assertEquals("soffid", result.get(0).getSystem());
        assertEquals("INV_SUPER per a SOFFID", result.get(0).getDescription());
    }

    @Test
    void getUserRoles_sendsAccountNameFilterExcludingTothom() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_ROLE_ACCOUNT_BODY)
                    .build());
        });

        client.getUserRoles("$kadadafas");

        String decodedUrl = URLDecoder.decode(capturedUrl[0], StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("accountName eq '$kadadafas'"), decodedUrl);
        assertTrue(decodedUrl.contains("roleName ne 'tothom'"), decodedUrl);
        assertTrue(capturedUrl[0].contains("/soffid/webservice/scim2/v1/RoleAccount"), capturedUrl[0]);
    }

    @Test
    void getUserRoles_emptyResources_returnsEmptyList() {
        String body = """
                {
                  "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
                  "totalResults": 0,
                  "Resources": []
                }
                """;
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build()));

        List<SoffidRole> result = client.getUserRoles("nobody");

        assertTrue(result.isEmpty());
    }

    @Test
    void getUserRoles_unauthorized_throwsIntegrationUnavailableException() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED).build()));

        assertThrows(IntegrationUnavailableException.class, () -> client.getUserRoles("$kadadafas"));
    }

    private static final String ONE_GROUP_BODY = """
            {
              "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
              "totalResults": 1,
              "Resources": [
                {
                  "id": 42,
                  "name": "sgaip",
                  "attributes": {
                    "DIR3": "A04027054 "
                  }
                }
              ]
            }
            """;

    @Test
    void resolveGroupDir3_groupFound_returnsTrimmedDir3Attribute() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(ONE_GROUP_BODY)
                .build()));

        String result = client.resolveGroupDir3("sgaip");

        assertEquals("A04027054", result);
    }

    @Test
    void resolveGroupDir3_sendsExactNameFilterAndSingleResultPaging() {
        String[] capturedUrl = new String[1];
        wireWebClient(request -> {
            capturedUrl[0] = request.url().toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ONE_GROUP_BODY)
                    .build());
        });

        client.resolveGroupDir3("sgaip");

        String decodedUrl = URLDecoder.decode(capturedUrl[0], StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("name eq 'sgaip'"), decodedUrl);
        assertTrue(capturedUrl[0].contains("startIndex=1"), capturedUrl[0]);
        assertTrue(capturedUrl[0].contains("count=1"), capturedUrl[0]);
    }

    @Test
    void resolveGroupDir3_groupHasNoAttributes_returnsNull() {
        String body = """
                {
                  "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
                  "totalResults": 1,
                  "Resources": [
                    {
                      "id": 99,
                      "name": "ncadmin"
                    }
                  ]
                }
                """;
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build()));

        assertNull(client.resolveGroupDir3("nonexistent"));
    }

    @Test
    void resolveGroupDir3_groupHasAttributesButNoDir3Key_returnsNull() {
        String body = """
                {
                  "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
                  "totalResults": 1,
                  "Resources": [
                    {
                      "id": 99,
                      "name": "ncadmin",
                      "attributes": {
                        "seccioPressupostaria": "1"
                      }
                    }
                  ]
                }
                """;
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build()));

        assertNull(client.resolveGroupDir3("nonexistent"));
    }

    @Test
    void resolveGroupDir3_noMatchingGroup_returnsNull() {
        String body = """
                {
                  "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
                  "totalResults": 0,
                  "Resources": []
                }
                """;
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(body)
                .build()));

        assertNull(client.resolveGroupDir3("nonexistent"));
    }

    @Test
    void resolveGroupDir3_unauthorized_throwsIntegrationUnavailableException() {
        wireWebClient(request -> Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED).build()));

        assertThrows(IntegrationUnavailableException.class, () -> client.resolveGroupDir3("sgaip"));
    }
}
