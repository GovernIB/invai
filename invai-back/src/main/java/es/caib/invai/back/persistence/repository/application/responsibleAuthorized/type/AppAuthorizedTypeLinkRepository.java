package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type;

import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;

import java.util.List;

/**
 * Persistence port abstraction decoupling the service layer from the JPA infrastructure used to
 * store the {@link AppAuthorizedTypeLink} intermediate join table. This join is never exposed
 * through its own REST controller: it is only manipulated internally by the anchor's service
 * facade. Audited and soft-deleted since 1.0.5.
 *
 * @since 1.0.3
 */
public interface AppAuthorizedTypeLinkRepository {

    /**
     * Persists a new authorization type join row and records an INSERT audit row.
     *
     * @param appAuthorizedTypeLink the join row to create
     * @return the persisted join row, including its generated identifier
     */
    AppAuthorizedTypeLink create(AppAuthorizedTypeLink appAuthorizedTypeLink);

    /**
     * Soft-deletes an authorization type join row (the given model's {@code deletedAt}/{@code
     * deletedBy} must already be set by the caller) and records a DELETE audit row.
     *
     * @param appAuthorizedTypeLink the join row to soft-delete
     */
    void delete(AppAuthorizedTypeLink appAuthorizedTypeLink);

    /**
     * Resolves all active (non soft-deleted) authorization type join rows attached to a given
     * authorized person anchor.
     *
     * @param appAuthorizedId the authorized person anchor identifier
     * @return the matching join rows
     */
    List<AppAuthorizedTypeLink> findAllActiveByAppAuthorizedId(Long appAuthorizedId);
}
