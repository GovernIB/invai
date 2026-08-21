package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.core;

import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;

/**
 * Persistence port abstraction decoupling the service layer from the JPA infrastructure used to
 * store the {@code AppResponsibleAuthorized} anchor entity.
 *
 * @since 1.0.3
 */
public interface AppResponsibleAuthorizedRepository {

    /**
     * Persists a new anchor record and writes the corresponding audit trail entry.
     *
     * @param appResponsibleAuthorized the anchor to create
     * @return the persisted anchor, including its generated identifier
     */
    AppResponsibleAuthorized create(AppResponsibleAuthorized appResponsibleAuthorized);

    /**
     * Merges changes into an existing anchor record and writes the corresponding audit trail entry.
     *
     * @param appResponsibleAuthorized the anchor data to merge
     * @param id identifier of the anchor to update
     * @return the updated anchor
     */
    AppResponsibleAuthorized update(AppResponsibleAuthorized appResponsibleAuthorized, Long id);

    /**
     * Soft-deletes an anchor record and writes the corresponding audit trail entry.
     *
     * @param appResponsibleAuthorized the anchor to delete
     */
    void delete(AppResponsibleAuthorized appResponsibleAuthorized);

    /**
     * Resolves an anchor record by its identifier.
     *
     * @param id the anchor identifier
     * @return the matching anchor, or {@code null} if not found
     */
    AppResponsibleAuthorized findById(Long id);

    /**
     * Resolves the anchor record scoped to a single parent application.
     *
     * @param applicationId the parent application identifier
     * @return the matching anchor, or {@code null} if not found
     */
    AppResponsibleAuthorized findByApplicationId(Long applicationId);
}
