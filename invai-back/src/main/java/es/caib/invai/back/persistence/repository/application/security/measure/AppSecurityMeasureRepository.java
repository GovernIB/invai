package es.caib.invai.back.persistence.repository.application.security.measure;

import es.caib.invai.back.service.model.application.security.measure.AppSecurityMeasure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port abstracting CRUD and paginated search operations for
 * {@link AppSecurityMeasure} aggregates.
 *
 * @since 1.0.4
 */
public interface AppSecurityMeasureRepository {

    /**
     * Persists a new security measure.
     *
     * @param appSecurityMeasure the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppSecurityMeasure create(AppSecurityMeasure appSecurityMeasure);

    /**
     * Updates the security measure identified by {@code id}.
     *
     * @param appSecurityMeasure the model carrying the updated values
     * @param id                 identifier of the record to update
     * @return the updated model
     */
    AppSecurityMeasure update(AppSecurityMeasure appSecurityMeasure, Long id);

    /**
     * Deletes (logically) the given security measure.
     *
     * @param appSecurityMeasure the model to delete
     */
    void delete(AppSecurityMeasure appSecurityMeasure);

    /**
     * Resolves a security measure by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppSecurityMeasure findById(Long id);

    /**
     * Resolves a paginated, filtered list of security measures scoped to a single parent
     * security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      additional filter criteria
     * @param pageable      pagination and sorting instructions
     * @return the paginated page of matching models
     */
    Page<AppSecurityMeasure> findAll(Long appSecurityId, AppSecurityMeasureCriteria criteria, Pageable pageable);
}
