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

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Calls the {@code invai-api-interna} module's Soffid endpoints on behalf of the person/role
 * lookup facades, forwarding the current session's Bearer token via
 * {@code IntegracionsClientConfig}. Keeps throwing {@link SoffidClientException}/
 * {@link SoffidTimeoutException} exactly as before, so downstream callers (including
 * {@code GlobalExceptionHandler} and {@code PersonService#checkDir3}) need no changes.
 *
 * @since 1.0.4
 */
@Component
@Slf4j
public class SoffidClient {

    /**
     * Deterministic per-request timeout applied on top of the underlying {@link WebClient}'s own
     * connect/response timeouts via {@code Mono#block(Duration)}, which always raises an
     * {@link IllegalStateException} on expiry. Callers rely on this to tell a timeout apart from
     * any other Soffid failure.
     */
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

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
                            .queryParam("search", search == null ? "" : search)
                            .queryParam("page", pageable.getPageNumber())
                            .queryParam("size", pageable.getPageSize())
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<SoffidPageResponse<SoffidUser>>() {
                    })
                    .block(REQUEST_TIMEOUT);

            List<SoffidUser> content = body != null && body.getContent() != null ? body.getContent() : List.of();
            long totalElements = body != null ? body.getTotalElements() : 0;
            return new PageImpl<>(content, pageable, totalElements);
        } catch (IllegalStateException ex) {
            log.error("Timed out searching Soffid users (search='{}')", search, ex);
            throw new SoffidTimeoutException();
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
                    .block(REQUEST_TIMEOUT);

            return response != null ? response.getBody() : null;
        } catch (IllegalStateException ex) {
            log.error("Timed out looking up Soffid user by email (email='{}')", email, ex);
            throw new SoffidTimeoutException();
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
                            .queryParam("name", name == null ? "" : name)
                            .queryParam("page", pageable.getPageNumber())
                            .queryParam("size", pageable.getPageSize())
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<SoffidPageResponse<SoffidRole>>() {
                    })
                    .block(REQUEST_TIMEOUT);

            List<SoffidRole> content = body != null && body.getContent() != null ? body.getContent() : List.of();
            long totalElements = body != null ? body.getTotalElements() : 0;
            return new PageImpl<>(content, pageable, totalElements);
        } catch (IllegalStateException ex) {
            log.error("Timed out searching Soffid roles (name='{}')", name, ex);
            throw new SoffidTimeoutException();
        } catch (WebClientResponseException ex) {
            log.error("Failed to search Soffid roles (name='{}')", name, ex);
            throw toSoffidException(ex);
        } catch (WebClientException ex) {
            log.error("Failed to search Soffid roles (name='{}')", name, ex);
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
                    .block(REQUEST_TIMEOUT);

            Map<String, String> body = response != null ? response.getBody() : null;
            return body != null ? body.get("dir3Code") : null;
        } catch (IllegalStateException ex) {
            log.error("Timed out resolving DIR3 for Soffid group (groupName='{}')", groupName, ex);
            throw new SoffidTimeoutException();
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
                    .block(REQUEST_TIMEOUT);

            List<SoffidRole> body = response != null ? response.getBody() : null;
            return body != null ? body : List.of();
        } catch (IllegalStateException ex) {
            log.error("Timed out fetching granted roles for Soffid account (username='{}')", username, ex);
            throw new SoffidTimeoutException();
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
                    .block(REQUEST_TIMEOUT);

            List<SoffidRole> body = response != null ? response.getBody() : null;
            return body != null ? body : List.of();
        } catch (IllegalStateException ex) {
            log.error("Timed out fetching Soffid roles by id (ids={})", ids, ex);
            throw new SoffidTimeoutException();
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
