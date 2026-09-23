package es.caib.invai.back.rest.soffid;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.List;

/**
 * Adapter implementing {@link SoffidClient} against the {@code invai-api-interna} module's Soffid
 * endpoints, forwarding the current session's Bearer token via {@code IntegracionsClientConfig}
 * instead of calling Soffid's SCIM 2.0 API directly.
 *
 * @since 1.0.4
 */
@Component
@Slf4j
public class SoffidClientAdapter implements SoffidClient {

    /** Pre-configured {@link WebClient} pointing at {@code invai-api-interna}, see {@code IntegracionsClientConfig}. */
    @Autowired
    @Qualifier("integracionsWebClient")
    private WebClient integracionsWebClient;

    @Override
    public Page<SoffidUser> search(String fullName, Pageable pageable) {
        try {
            SoffidPageResponse<SoffidUser> body = integracionsWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/soffid/users")
                            .queryParam("search", fullName == null ? "" : fullName)
                            .queryParam("page", pageable.getPageNumber())
                            .queryParam("size", pageable.getPageSize())
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<SoffidPageResponse<SoffidUser>>() {
                    })
                    .block();

            List<SoffidUser> content = body != null && body.getContent() != null ? body.getContent() : List.of();
            long totalElements = body != null ? body.getTotalElements() : 0;
            return new PageImpl<>(content, pageable, totalElements);
        } catch (WebClientException ex) {
            log.error("Failed to search Soffid users (fullName='{}')", fullName, ex);
            throw new BusinessRuleException(Constants.ERR_SOFFID_UNAVAILABLE);
        }
    }

    @Override
    public SoffidUser findByEmail(String email) {
        try {
            ResponseEntity<SoffidUser> response = integracionsWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/soffid/users/by-email")
                            .queryParam("email", email)
                            .build())
                    .retrieve()
                    .toEntity(SoffidUser.class)
                    .block();

            return response != null ? response.getBody() : null;
        } catch (WebClientException ex) {
            log.error("Failed to look up Soffid user by email (email='{}')", email, ex);
            throw new BusinessRuleException(Constants.ERR_SOFFID_UNAVAILABLE);
        }
    }

    @Override
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
                    .block();

            List<SoffidRole> content = body != null && body.getContent() != null ? body.getContent() : List.of();
            long totalElements = body != null ? body.getTotalElements() : 0;
            return new PageImpl<>(content, pageable, totalElements);
        } catch (WebClientException ex) {
            log.error("Failed to search Soffid roles (name='{}')", name, ex);
            throw new BusinessRuleException(Constants.ERR_SOFFID_UNAVAILABLE);
        }
    }
}
