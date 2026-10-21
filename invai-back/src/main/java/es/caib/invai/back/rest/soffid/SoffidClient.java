package es.caib.invai.back.rest.soffid;

import es.caib.invai.back.exception.SoffidClientException;
import es.caib.invai.back.exception.SoffidTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Calls the {@code invai-api-interna} module's Soffid endpoints on behalf of the person/role
 * lookup facades, forwarding the current session's Bearer token via
 * {@code IntegracionsClientConfig}. Keeps throwing {@link SoffidClientException}/
 * {@link SoffidTimeoutException} exactly as before, so downstream callers (including
 * {@code GlobalExceptionHandler} and {@code PersonService#checkDir3}) need no changes.
 * <p>
 * This client applies no timeout of its own to Soffid: {@code invai-api-interna} does, and answers
 * a 504 when Soffid takes too long, which is what becomes a {@link SoffidTimeoutException} (see
 * {@code toSoffidException}). The HTTP timeouts of {@code IntegracionsClientConfig} are only a
 * safety net for {@code invai-api-interna} itself not answering.
 * </p>
 *
 * @since 1.0.4
 */
@Component
@Slf4j
public class SoffidClient {

    /** Pre-configured {@link WebClient} pointing at {@code invai-api-interna}, see {@code IntegracionsClientConfig}. */
    @Autowired
    @Qualifier("integracionsWebClient")
    private WebClient integracionsWebClient;

    /**
     * Lists active Soffid users, optionally restricted to those where every word of {@code search}
     * appears in either the full name or the username.
     *
     * @param search   the text to search for, or {@code null}/blank to list every active user
     * @param pageable the pagination parameters; only page number and size are used
     * @return the requested page of matching Soffid users, with the total result count
     */
    public Page<SoffidUser> searchUsers(String search, Pageable pageable) {
        try {
            SoffidPageResponse<SoffidUser> body = integracionsWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/soffid/users")
                            .queryParam("search", Objects.isNull(search) ? "" : search)
                            .queryParam("page", pageable.getPageNumber())
                            .queryParam("size", pageable.getPageSize())
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<SoffidPageResponse<SoffidUser>>() {
                    })
                    .block();

            List<SoffidUser> content = Objects.nonNull(body) && Objects.nonNull(body.getContent()) ? body.getContent() : List.of();
            long totalElements = Objects.nonNull(body) ? body.getTotalElements() : 0;
            return new PageImpl<>(content, pageable, totalElements);
        } catch (WebClientResponseException ex) {
            log.error("Failed to search Soffid users (search='{}')", search, ex);
            throw toSoffidException(ex);
        } catch (WebClientException ex) {
            log.error("Failed to search Soffid users (search='{}')", search, ex);
            throw new SoffidClientException(ex.getClass().getSimpleName());
        }
    }

    /**
     * Looks up a single active Soffid user by exact e-mail address.
     *
     * @param email the exact e-mail address to look up
     * @return the matching active Soffid user, or {@code null} if none is found
     */
    public SoffidUser searchByEmail(String email) {
        try {
            ResponseEntity<SoffidUser> response = integracionsWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/soffid/users/by-email")
                            .queryParam("email", email)
                            .build())
                    .retrieve()
                    .toEntity(SoffidUser.class)
                    .block();

            return Objects.nonNull(response) ? response.getBody() : null;
        } catch (WebClientResponseException ex) {
            log.error("Failed to look up Soffid user by email (email='{}')", email, ex);
            throw toSoffidException(ex);
        } catch (WebClientException ex) {
            log.error("Failed to look up Soffid user by email (email='{}')", email, ex);
            throw new SoffidClientException(ex.getClass().getSimpleName());
        }
    }

    /**
     * Lists Soffid roles that can be assigned to an application, optionally restricted to those
     * where every word of {@code name} is contained in the role's {@code name}.
     *
     * @param name     the text to search for, or {@code null}/blank to list every role
     * @param pageable the pagination parameters; only page number and size are used
     * @return the requested page of matching Soffid roles, with the total result count
     */
    public Page<SoffidRole> searchRoles(String name, Pageable pageable) {
        try {
            SoffidPageResponse<SoffidRole> body = integracionsWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/soffid/roles")
                            .queryParam("name", Objects.isNull(name) ? "" : name)
                            .queryParam("page", pageable.getPageNumber())
                            .queryParam("size", pageable.getPageSize())
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<SoffidPageResponse<SoffidRole>>() {
                    })
                    .block();

            List<SoffidRole> content = Objects.nonNull(body) && Objects.nonNull(body.getContent()) ? body.getContent() : List.of();
            long totalElements = Objects.nonNull(body) ? body.getTotalElements() : 0;
            return new PageImpl<>(content, pageable, totalElements);
        } catch (WebClientResponseException ex) {
            log.error("Failed to search Soffid roles (name='{}')", name, ex);
            throw toSoffidException(ex);
        } catch (WebClientException ex) {
            log.error("Failed to search Soffid roles (name='{}')", name, ex);
            throw new SoffidClientException(ex.getClass().getSimpleName());
        }
    }

    /**
     * Look up, in a single call, the active Soffid users having any of the given e-mail addresses
     * - The bulk counterpart of {@link #searchByEmail}. {@code invai-api-interna} rejects more than
     * 100 addresses per call, and Soffid only accepts a few dozen, so keep the batch small.
     *
     * @param emails the exact e-mail addresses to look up, never empty
     * @return the active users found - fewer than requested if some address has no active user
     */
    public List<SoffidUser> getUsersByEmails(List<String> emails) {
        try {
            ResponseEntity<List<SoffidUser>> response = integracionsWebClient.post()
                    .uri("/soffid/users/by-emails")
                    .bodyValue(emails)
                    .retrieve()
                    .toEntityList(SoffidUser.class)
                    .block();

            List<SoffidUser> body = Objects.nonNull(response) ? response.getBody() : null;
            return Objects.nonNull(body) ? body : List.of();
        } catch (WebClientResponseException ex) {
            log.error("Failed to fetch Soffid users by e-mail (batch of {})", emails.size(), ex);
            throw toSoffidException(ex);
        } catch (WebClientException ex) {
            log.error("Failed to fetch Soffid users by e-mail (batch of {})", emails.size(), ex);
            throw new SoffidClientException(ex.getClass().getSimpleName());
        }
    }

    /**
     * Gets the DIR3CAIB code of every Soffid group that has one, in a single call - there are only a
     * handful of them among thousands of groups.
     *
     * @return the DIR3CAIB code of every group having one, keyed by the group's short name
     */
    public Map<String, String> getGroupsDir3() {
        try {
            ResponseEntity<Map<String, String>> response = integracionsWebClient.get()
                    .uri("/soffid/groups/dir3")
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<Map<String, String>>() {
                    })
                    .block();

            Map<String, String> body = Objects.nonNull(response) ? response.getBody() : null;
            return Objects.nonNull(body) ? body : Map.of();
        } catch (WebClientResponseException ex) {
            log.error("Failed to fetch the Soffid groups with a DIR3", ex);
            throw toSoffidException(ex);
        } catch (WebClientException ex) {
            log.error("Failed to fetch the Soffid groups with a DIR3", ex);
            throw new SoffidClientException(ex.getClass().getSimpleName());
        }
    }

    /**
     * Resolves the DIR3CAIB code registered on a Soffid group.
     *
     * @param groupName the Soffid group's short internal name to look up (e.g. {@code "sgaip"})
     * @return the group's DIR3CAIB code, or {@code null} if no such group exists, or it carries no
     * {@code "DIR3"} attribute
     */
    public String resolveGroupDir3(String groupName) {
        try {
            ResponseEntity<Map<String, String>> response = integracionsWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/soffid/groups/{groupName}/dir3")
                            .build(groupName))
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<Map<String, String>>() {
                    })
                    .block();

            Map<String, String> body = Objects.nonNull(response) ? response.getBody() : null;
            return Objects.nonNull(body) ? body.get("dir3Code") : null;
        } catch (WebClientResponseException ex) {
            log.error("Failed to resolve DIR3 for Soffid group (groupName='{}')", groupName, ex);
            throw toSoffidException(ex);
        } catch (WebClientException ex) {
            log.error("Failed to resolve DIR3 for Soffid group (groupName='{}')", groupName, ex);
            throw new SoffidClientException(ex.getClass().getSimpleName());
        }
    }

    /**
     * Fetches the roles Soffid currently reports as granted to the given account.
     *
     * @param username the Soffid account name to look up granted roles for
     * @return every role currently granted to {@code username}, or an empty list if none
     */
    public List<SoffidRole> getGrantedRoles(String username) {
        try {
            ResponseEntity<List<SoffidRole>> response = integracionsWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/soffid/users/{username}/roles").build(username))
                    .retrieve()
                    .toEntityList(SoffidRole.class)
                    .block();

            List<SoffidRole> body = Objects.nonNull(response) ? response.getBody() : null;
            return Objects.nonNull(body) ? body : List.of();
        } catch (WebClientResponseException ex) {
            log.error("Failed to fetch granted roles for Soffid account (username='{}')", username, ex);
            throw toSoffidException(ex);
        } catch (WebClientException ex) {
            log.error("Failed to fetch granted roles for Soffid account (username='{}')", username, ex);
            throw new SoffidClientException(ex.getClass().getSimpleName());
        }
    }

    /**
     * Resolves the current Soffid role details for a given set of role ids.
     *
     * @param ids the Soffid role ids to resolve, never empty
     * @return every role Soffid currently has for the given ids - fewer than requested if some id
     * no longer exists
     */
    public List<SoffidRole> getRolesByIds(List<Long> ids) {
        try {
            ResponseEntity<List<SoffidRole>> response = integracionsWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/soffid/roles/by-ids").queryParam("ids", ids).build())
                    .retrieve()
                    .toEntityList(SoffidRole.class)
                    .block();

            List<SoffidRole> body = Objects.nonNull(response) ? response.getBody() : null;
            return Objects.nonNull(body) ? body : List.of();
        } catch (WebClientResponseException ex) {
            log.error("Failed to fetch Soffid roles by id (ids={})", ids, ex);
            throw toSoffidException(ex);
        } catch (WebClientException ex) {
            log.error("Failed to fetch Soffid roles by id (ids={})", ids, ex);
            throw new SoffidClientException(ex.getClass().getSimpleName());
        }
    }

    /**
     * Maps a non-2xx response from {@code invai-api-interna} to the exception type its status code
     * represents: a 504 (raised by {@code IntegrationTimeoutException} there) means Soffid itself
     * timed out, everything else means Soffid is unavailable for some other reason.
     *
     * @param ex the response exception raised by the call to {@code invai-api-interna}
     * @return the equivalent {@link SoffidTimeoutException} or {@link SoffidClientException}
     */
    private static RuntimeException toSoffidException(WebClientResponseException ex) {
        if (ex.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
            return new SoffidTimeoutException();
        }
        return new SoffidClientException(String.valueOf(ex.getStatusCode().value()));
    }
}
