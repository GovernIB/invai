package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type;

import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;

import java.util.List;

/**
 * Persistence port abstraction decoupling the service layer from the JPA infrastructure used to
 * store the {@link AppAuthorizedTypeLink} intermediate join table. This join is never exposed
 * through its own REST controller: it is only manipulated internally by the anchor's service
 * facade. Deliberately lightweight: {@code delete} is a plain hard delete, there is no
 * soft-delete/audit trail on this table.
 *
 * @since 1.0.3
 */
public interface AppAuthorizedTypeLinkRepository {

    /**
     * Persists a new authorization type join row.
     *
     * @param appAuthorizedTypeLink the join row to create
     * @return the persisted join row, including its generated identifier
     */
    AppAuthorizedTypeLink create(AppAuthorizedTypeLink appAuthorizedTypeLink);

    /**
     * Permanently removes an authorization type join row (hard delete, no audit trail).
     *
     * @param appAuthorizedTypeLink the join row to remove
     */
    void delete(AppAuthorizedTypeLink appAuthorizedTypeLink);

    /**
     * Resolves all authorization type join rows attached to a given authorized person anchor.
     *
     * @param appAuthorizedId the authorized person anchor identifier
     * @return the matching join rows
     */
    List<AppAuthorizedTypeLink> findAllByAppAuthorizedId(Long appAuthorizedId);
}
