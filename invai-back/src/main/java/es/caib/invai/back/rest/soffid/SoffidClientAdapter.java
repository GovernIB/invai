package es.caib.invai.back.rest.soffid;

import es.caib.invai.back.config.SoffidConfig;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter implementing {@link SoffidClient} against the real Soffid SCIM 2.0 API: an unfiltered,
 * paginated listing of active users when no full name is given, or one restricted to users whose
 * full name contains every word of the given text.
 *
 * @since 1.0.4
 */
@Component
@Slf4j
public class SoffidClientAdapter implements SoffidClient {

    /** WebClient pre-configured with the Soffid base URL and HTTP Basic Auth credentials. */
    @Autowired
    private WebClient soffidWebClient;

    /** Holds the configured SCIM endpoint paths, retrieved via {@link SoffidConfig#getSearchPath()}/{@link SoffidConfig#getRoleSearchPath()}. */
    @Autowired
    private SoffidConfig soffidConfig;

    @Override
    public Page<SoffidUser> search(String fullName, Pageable pageable) {
        String filter = buildFilter(fullName);
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
                    .block();

            SoffidUserListResponse body = response != null ? response.getBody() : null;
            List<SoffidUser> resources = body != null && body.getResources() != null ? body.getResources() : List.of();
            long totalResults = body != null ? body.getTotalResults() : 0;
            return new PageImpl<>(resources, pageable, totalResults);
        } catch (WebClientException ex) {
            log.error("Failed to search Soffid users (fullName='{}')", fullName, ex);
            throw new BusinessRuleException(Constants.ERR_SOFFID_UNAVAILABLE);
        }
    }

    @Override
    public SoffidUser findByEmail(String email) {
        String filter = "emailAddress eq '" + escape(email) + "' and active eq true";
        try {
            ResponseEntity<SoffidUserListResponse> response = soffidWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(soffidConfig.getSearchPath())
                            .queryParam("filter", filter)
                            .queryParam("startIndex", 1)
                            .queryParam("count", 1)
                            .build())
                    .retrieve()
                    .toEntity(SoffidUserListResponse.class)
                    .block();

            SoffidUserListResponse body = response != null ? response.getBody() : null;
            List<SoffidUser> resources = body != null ? body.getResources() : null;
            return resources != null && !resources.isEmpty() ? resources.get(0) : null;
        } catch (WebClientException ex) {
            log.error("Failed to look up Soffid user by email (email='{}')", email, ex);
            throw new BusinessRuleException(Constants.ERR_SOFFID_UNAVAILABLE);
        }
    }

    @Override
    public Page<SoffidRole> searchRoles(String name, Pageable pageable) {
        int startIndex = pageable.getPageNumber() * pageable.getPageSize() + 1;
        try {
            ResponseEntity<SoffidRoleListResponse> response = soffidWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(soffidConfig.getRoleSearchPath())
                            .queryParam("filter", buildRoleFilter(name))
                            .queryParam("startIndex", startIndex)
                            .queryParam("count", pageable.getPageSize())
                            .build())
                    .retrieve()
                    .toEntity(SoffidRoleListResponse.class)
                    .block();

            SoffidRoleListResponse body = response != null ? response.getBody() : null;
            List<SoffidRole> resources = body != null && body.getResources() != null ? body.getResources() : List.of();
            long totalResults = body != null ? body.getTotalResults() : 0;
            return new PageImpl<>(resources, pageable, totalResults);
        } catch (WebClientException ex) {
            log.error("Failed to search Soffid roles (name='{}')", name, ex);
            throw new BusinessRuleException(Constants.ERR_SOFFID_UNAVAILABLE);
        }
    }

    /**
     * Builds the SCIM filter restricting Soffid roles to those belonging to this project's
     * namespace, always requiring {@code name} to start with {@code "INV_"} - the shared Soffid
     * instance hosts roles for many unrelated applications, so this base clause is never omitted.
     * On top of that base clause, every whitespace-separated word of the given {@code name} search
     * text must additionally appear somewhere in the role's {@code name} field — independently of
     * order, mirroring {@link #buildFilter(String)}'s word-matching semantics for users. A
     * blank/{@code null} {@code name} yields just the base {@code "INV_"} clause, i.e. every role
     * in this project's namespace is listed.
     *
     * @param name the raw search text, one or more words, or {@code null}/blank to list every
     * {@code INV_} role
     * @return the SCIM {@code filter} expression, always scoped to the {@code INV_} namespace
     */
    private static String buildRoleFilter(String name) {
        String baseFilter = "name sw 'INV_'";
        if (name == null || name.isBlank()) {
            return baseFilter;
        }
        String wordClauses = Arrays.stream(name.trim().split("\\s+"))
                .map(word -> "name co '" + escape(word) + "'")
                .collect(Collectors.joining(" and "));
        return baseFilter + " and " + wordClauses;
    }

    /**
     * Builds the SCIM filter: always restricted to active accounts, additionally requiring every
     * whitespace-separated word of {@code fullName} to appear somewhere in the user's
     * {@code fullName} field when one is given — independently of order, e.g. {@code "Gabriel
     * Perez"} requires "Gabriel" and "Perez" to each appear, even though they aren't adjacent (e.g.
     * "Gabriel Lorenzo Perez Molina"). Matching against {@code fullName} rather than
     * {@code firstName}/{@code lastName} individually also covers middle names. A blank/{@code
     * null} {@code fullName} yields no name clause at all, i.e. an unfiltered active-user listing.
     *
     * @param fullName the raw search text, one or more words, or {@code null}/blank for no filter
     * @return the SCIM {@code filter} expression, restricted to active accounts
     */
    private static String buildFilter(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "active eq true";
        }
        String perWordClauses = Arrays.stream(fullName.trim().split("\\s+"))
                .map(word -> "fullName co '" + escape(word) + "'")
                .collect(Collectors.joining(" and "));
        return perWordClauses + " and active eq true";
    }

    /**
     * Escapes single quotes in a SCIM filter value, since the filter is built via string
     * concatenation and single quotes delimit string literals in SCIM filter syntax.
     *
     * @param value the raw value to escape
     * @return the escaped value, safe to embed inside a single-quoted SCIM filter literal
     */
    private static String escape(String value) {
        return value.replace("'", "\\'");
    }
}
