package es.caib.invai.api.interna.rest.soffid;

import es.caib.invai.api.interna.config.SoffidConfig;
import es.caib.invai.api.interna.controller.soffid.SoffidController;
import es.caib.invai.api.interna.exception.IntegrationTimeoutException;
import es.caib.invai.api.interna.exception.IntegrationUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calls the real Soffid SCIM 2.0 API on behalf of {@link SoffidController}: an unfiltered,
 * paginated listing of active users when no search text is given, or one restricted (in a single
 * SCIM request) to users where every word of the given text appears in either the full name or
 * the username (código de usuario) — so a single search box finds someone by name or by user code
 * without the caller needing to know which one they typed, and without a second round trip.
 *
 * @since 1.0.5
 */
@Component
@Slf4j
public class SoffidClient {

    /**
     * Deterministic per-request timeout applied on top of {@link WebClient}'s own connect/response
     * timeouts (configured in {@link SoffidConfig#soffidWebClient()}) via {@code Mono#block(Duration)},
     * which always raises an {@link IllegalStateException} on expiry - unlike the underlying HTTP
     * client timeouts, whose exact exception shape after a plain {@code .block()} isn't guaranteed
     * to be reliably distinguishable. Callers rely on this to tell a timeout apart from any other
     * Soffid failure.
     */
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    /** WebClient pre-configured with the Soffid base URL and HTTP Basic Auth credentials. */
    @Autowired
    private WebClient soffidWebClient;

    /** Holds the configured SCIM endpoint paths, retrieved via {@link SoffidConfig#getSearchPath()}/{@link SoffidConfig#getRoleSearchPath()}. */
    @Autowired
    private SoffidConfig soffidConfig;

    /**
     * Lists active Soffid users, optionally restricted to those matching every word of {@code
     * search} in either the full name or the username.
     *
     * @param search   the text to search for, or {@code null}/blank to list every active user
     * @param pageable the pagination parameters
     * @return the requested page of matching Soffid users, with the total result count
     */
    public Page<SoffidUser> searchUsers(String search, Pageable pageable) {
        String filter = buildFilter(search);
        int startIndex = pageable.getPageNumber() * pageable.getPageSize() + 1;
        try {
            ResponseEntity<SoffidUserListResponse> response = soffidWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(soffidConfig.getSearchPath())
                            .queryParam("filter", filter)
                            .queryParam("startIndex", startIndex)
                            .queryParam("count", pageable.getPageSize())
                            .build())
                    .retrieve()
                    .toEntity(SoffidUserListResponse.class)
                    .block(REQUEST_TIMEOUT);

            SoffidUserListResponse body = response != null ? response.getBody() : null;
            List<SoffidUser> resources = body != null && body.getResources() != null ? body.getResources() : List.of();
            long totalResults = body != null ? body.getTotalResults() : 0;
            return new PageImpl<>(resources, pageable, totalResults);
        } catch (IllegalStateException ex) {
            log.error("Timed out searching Soffid users (search='{}')", search, ex);
            throw new IntegrationTimeoutException("Soffid timed out", ex);
        } catch (WebClientException ex) {
            log.error("Failed to search Soffid users (search='{}')", search, ex);
            throw new IntegrationUnavailableException("Soffid unavailable", ex);
        }
    }

    /**
     * Looks up a single active Soffid user by exact e-mail address.
     *
     * @param email the exact e-mail address to look up
     * @return the matching active Soffid user, or {@code null} if none is found
     */
    public SoffidUser searchByEmail(String email) {
        String filter = "emailAddress eq '" + escape(email) + "' and active eq true";
        try {
            ResponseEntity<SoffidUserListResponse> response = soffidWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(soffidConfig.getSearchPath())
                            .queryParam("filter", filter)
                            .build())
                    .retrieve()
                    .toEntity(SoffidUserListResponse.class)
                    .block(REQUEST_TIMEOUT);

            SoffidUserListResponse body = response != null ? response.getBody() : null;
            List<SoffidUser> resources = body != null ? body.getResources() : null;
            return resources != null && !resources.isEmpty() ? resources.get(0) : null;
        } catch (IllegalStateException ex) {
            log.error("Timed out looking up Soffid user by email (email='{}')", email, ex);
            throw new IntegrationTimeoutException("Soffid timed out", ex);
        } catch (WebClientException ex) {
            log.error("Failed to look up Soffid user by email (email='{}')", email, ex);
            throw new IntegrationUnavailableException("Soffid unavailable", ex);
        }
    }

    /**
     * Lists Soffid roles that can be assigned to an application, optionally restricted to those
     * matching every word of {@code name}.
     *
     * @param name     the text to search for, or {@code null}/blank to list every role
     * @param pageable the pagination parameters
     * @return the requested page of matching Soffid roles, with the total result count
     */
    public Page<SoffidRole> searchRoles(String name, Pageable pageable) {
        int startIndex = pageable.getPageNumber() * pageable.getPageSize() + 1;
        try {
            String filter = buildRoleFilter(name);
            ResponseEntity<SoffidRoleListResponse> response = soffidWebClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(soffidConfig.getRoleSearchPath())
                                .queryParam("startIndex", startIndex)
                                .queryParam("count", pageable.getPageSize());
                        if (filter != null) {
                            uriBuilder.queryParam("filter", filter);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .toEntity(SoffidRoleListResponse.class)
                    .block(REQUEST_TIMEOUT);

            SoffidRoleListResponse body = response != null ? response.getBody() : null;
            List<SoffidRole> resources = body != null && body.getResources() != null ? body.getResources() : List.of();
            long totalResults = body != null ? body.getTotalResults() : 0;
            return new PageImpl<>(resources, pageable, totalResults);
        } catch (IllegalStateException ex) {
            log.error("Timed out searching Soffid roles (name='{}')", name, ex);
            throw new IntegrationTimeoutException("Soffid timed out", ex);
        } catch (WebClientException ex) {
            log.error("Failed to search Soffid roles (name='{}')", name, ex);
            throw new IntegrationUnavailableException("Soffid unavailable", ex);
        }
    }

    /**
     * Resolves the current Soffid role details for a given set of role ids - used to build/refresh
     * the persisted snapshot of an integration entry's required roles from just the ids the
     * frontend sends (see {@code AppIntegrationConnectionServiceFacadeBean#getRequiredRoles} in
     * {@code invai-back}), rather than trusting client-supplied name/system/description.
     *
     * @param ids the Soffid role ids to resolve, never empty
     * @return every role Soffid currently has for the given ids - fewer than requested if some id
     * no longer exists
     */
    public List<SoffidRole> getRolesByIds(List<Long> ids) {
        String filter = ids.stream().map(id -> "id eq " + id).collect(Collectors.joining(" or "));
        try {
            ResponseEntity<SoffidRoleListResponse> response = soffidWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(soffidConfig.getRoleSearchPath())
                            .queryParam("filter", filter)
                            .queryParam("count", ids.size())
                            .build())
                    .retrieve()
                    .toEntity(SoffidRoleListResponse.class)
                    .block(REQUEST_TIMEOUT);

            SoffidRoleListResponse body = response != null ? response.getBody() : null;
            return body != null && body.getResources() != null ? body.getResources() : List.of();
        } catch (IllegalStateException ex) {
            log.error("Timed out fetching Soffid roles by id (ids={})", ids, ex);
            throw new IntegrationTimeoutException("Soffid timed out", ex);
        } catch (WebClientException ex) {
            log.error("Failed to fetch Soffid roles by id (ids={})", ids, ex);
            throw new IntegrationUnavailableException("Soffid unavailable", ex);
        }
    }

    /**
     * Resolves the DIR3CAIB code registered on a Soffid group.
     *
     * @param groupName the Soffid group's short internal name to look up (e.g. {@code "sgaip"})
     * @return the group's DIR3CAIB code (trimmed), or {@code null} if no such group exists, or it
     * carries no {@code "DIR3"} attribute
     */
    public String resolveGroupDir3(String groupName) {
        String filter = "name eq '" + escape(groupName) + "'";
        try {
            ResponseEntity<SoffidGroupListResponse> response = soffidWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(soffidConfig.getGroupSearchPath())
                            .queryParam("filter", filter)
                            .queryParam("startIndex", 1)
                            .queryParam("count", 1)
                            .build())
                    .retrieve()
                    .toEntity(SoffidGroupListResponse.class)
                    .block(REQUEST_TIMEOUT);

            SoffidGroupListResponse body = response != null ? response.getBody() : null;
            List<SoffidGroup> resources = body != null ? body.getResources() : null;
            if (resources == null || resources.isEmpty()) {
                return null;
            }
            Map<String, Object> attributes = resources.get(0).getAttributes();
            Object dir3 = attributes != null ? attributes.get("DIR3") : null;
            return dir3 != null ? dir3.toString().trim() : null;
        } catch (IllegalStateException ex) {
            log.error("Timed out resolving DIR3 for Soffid group (groupName='{}')", groupName, ex);
            throw new IntegrationTimeoutException("Soffid timed out", ex);
        } catch (WebClientException ex) {
            log.error("Failed to resolve DIR3 for Soffid group (groupName='{}')", groupName, ex);
            throw new IntegrationUnavailableException("Soffid unavailable", ex);
        }
    }

    /**
     * Fetches the roles Soffid currently reports as granted to the given account - used to compare
     * against an integration entry's persisted required roles and flag a live warning when they
     * differ (see {@code
     * es.caib.invai.back.ejb.application.integration.connection.AppIntegrationConnectionServiceFacadeBean}).
     * The {@code tothom} ("everyone") base role every account holds is excluded, since it's never a
     * meaningful required role.
     *
     * @param username the Soffid account name to look up granted roles for
     * @return every role currently granted to {@code username} (excluding {@code tothom}), or an
     * empty list if none
     */
    public List<SoffidRole> getUserRoles(String username) {
        String filter = "accountName eq '" + escape(username) + "' and roleName ne 'tothom'";
        try {
            ResponseEntity<SoffidUserRoleListResponse> response = soffidWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(soffidConfig.getGrantedRolePath())
                            .queryParam("filter", filter)
                            .build())
                    .retrieve()
                    .toEntity(SoffidUserRoleListResponse.class)
                    .block(REQUEST_TIMEOUT);

            SoffidUserRoleListResponse body = response != null ? response.getBody() : null;
            List<SoffidUserRole> resources = body != null ? body.getResources() : null;
            return resources != null ? resources.stream().map(SoffidClient::toSoffidRole).toList() : List.of();
        } catch (IllegalStateException ex) {
            log.error("Timed out fetching granted roles for Soffid account (username='{}')", username, ex);
            throw new IntegrationTimeoutException("Soffid timed out", ex);
        } catch (WebClientException ex) {
            log.error("Failed to fetch granted roles for Soffid account (username='{}')", username, ex);
            throw new IntegrationUnavailableException("Soffid unavailable", ex);
        }
    }

    /**
     * Converts a {@code UserRole} resource into the same {@link SoffidRole} shape used
     * elsewhere, so callers compare/display granted and required roles identically.
     *
     * @param userRole the granted-role resource to convert
     * @return the equivalent {@link SoffidRole}
     */
    private static SoffidRole toSoffidRole(SoffidUserRole userRole) {
        SoffidRole role = new SoffidRole();
        role.setId(userRole.getRoleId());
        role.setName(userRole.getRoleName());
        role.setSystem(userRole.getSystem());
        role.setDescription(userRole.getRoleDescription());
        return role;
    }

    /**
     * Builds the SCIM filter for role search: every whitespace-separated word of {@code name}
     * must appear somewhere in the role's {@code name} field.
     *
     * @param name the raw search text, one or more words, or {@code null}/blank for no filter
     * @return the SCIM {@code filter} expression, or {@code null} if {@code name} is blank
     */
    private static String buildRoleFilter(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return Arrays.stream(name.trim().split("\\s+"))
                .map(word -> "name co '" + escape(word) + "'")
                .collect(Collectors.joining(" and "));
    }

    /**
     * Builds the SCIM filter: always restricted to active accounts, additionally requiring every
     * whitespace-separated word of {@code search} to appear somewhere in the user's {@code
     * fullName}, their {@code userName} (código de usuario) or their {@code emailAddress} - the
     * same three fields the local {@code Person} catalog search matches against (see {@code
     * PersonSpecification} in {@code invai-back}), so both sources behave consistently from the
     * caller's point of view.
     *
     * @param search the raw search text, one or more words, or {@code null}/blank for no filter
     * @return the SCIM {@code filter} expression, restricted to active accounts
     */
    private static String buildFilter(String search) {
        if (StringUtils.isBlank(search)) {
            return "active eq true";
        }
        String perWordClauses = Arrays.stream(search.trim().split("\\s+"))
                .map(word -> "(fullName co '" + escape(word) + "' or userName co '" + escape(word)
                        + "' or emailAddress co '" + escape(word) + "')")
                .collect(Collectors.joining(" and "));
        return perWordClauses + " and active eq true";
    }

    /**
     * Escapes single quotes in an SCIM filter value, since the filter is built via string
     * concatenation and single quotes delimit string literals in SCIM filter syntax.
     *
     * @param value the raw value to escape
     * @return the escaped value, safe to embed inside a single-quoted SCIM filter literal
     */
    private static String escape(String value) {
        return value.replace("'", "\\'");
    }
}
