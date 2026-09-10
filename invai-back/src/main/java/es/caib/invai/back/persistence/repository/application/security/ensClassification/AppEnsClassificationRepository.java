package es.caib.invai.back.persistence.repository.application.security.ensClassification;

import es.caib.invai.back.service.model.application.security.ensClassification.AppEnsClassification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port abstracting CRUD and paginated search operations for
 * {@link AppEnsClassification} aggregates.
 *
 * @since 1.0.4
 */
public interface AppEnsClassificationRepository {

    /**
     * Persists a new ENS classification record.
     *
     * @param appEnsClassification the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppEnsClassification create(AppEnsClassification appEnsClassification);

    /**
     * Updates the ENS classification record identified by {@code id}.
     *
     * @param appEnsClassification the model carrying the updated values
     * @param id                   identifier of the record to update
     * @return the updated model
     */
    AppEnsClassification update(AppEnsClassification appEnsClassification, Long id);

    /**
     * Deletes (logically) the given ENS classification record.
     *
     * @param appEnsClassification the model to delete
     */
    void delete(AppEnsClassification appEnsClassification);

    /**
     * Resolves an ENS classification record by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppEnsClassification findById(Long id);

    /**
     * Resolves a paginated, filtered list of ENS classification records scoped to a
     * single parent security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      additional filter criteria
     * @param pageable      pagination and sorting instructions
     * @return the paginated page of matching models
     */
    Page<AppEnsClassification> findAll(Long appSecurityId, AppEnsClassificationCriteria criteria, Pageable pageable);
}
