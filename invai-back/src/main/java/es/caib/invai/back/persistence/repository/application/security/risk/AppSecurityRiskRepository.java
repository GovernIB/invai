package es.caib.invai.back.persistence.repository.application.security.risk;

import es.caib.invai.back.service.model.application.security.risk.AppSecurityRisk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port abstracting CRUD and paginated search operations for
 * {@link AppSecurityRisk} aggregates.
 *
 * @since 1.0.4
 */
public interface AppSecurityRiskRepository {

    /**
     * Persists a new security risk.
     *
     * @param appSecurityRisk the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppSecurityRisk create(AppSecurityRisk appSecurityRisk);

    /**
     * Updates the security risk identified by {@code id}.
     *
     * @param appSecurityRisk the model carrying the updated values
     * @param id              identifier of the record to update
     * @return the updated model
     */
    AppSecurityRisk update(AppSecurityRisk appSecurityRisk, Long id);

    /**
     * Deletes (logically) the given security risk.
     *
     * @param appSecurityRisk the model to delete
     */
    void delete(AppSecurityRisk appSecurityRisk);

    /**
     * Resolves a security risk by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppSecurityRisk findById(Long id);

    /**
     * Resolves a paginated, filtered list of security risks scoped to a
     * single parent security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      additional filter criteria
     * @param pageable      pagination and sorting instructions
     * @return the paginated page of matching models
     */
    Page<AppSecurityRisk> findAll(Long appSecurityId, AppSecurityRiskCriteria criteria, Pageable pageable);
}
