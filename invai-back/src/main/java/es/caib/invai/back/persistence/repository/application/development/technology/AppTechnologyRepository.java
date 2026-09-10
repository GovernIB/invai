package es.caib.invai.back.persistence.repository.application.development.technology;

import es.caib.invai.back.service.model.application.development.technology.AppTechnology;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository contract for persisting and querying {@link AppTechnology} records. See
 * {@link AppTechnologyRepositoryAdapter} for the implementation, which also writes a
 * corresponding audit trail row on every write.
 *
 * @since 1.0.2
 */
public interface AppTechnologyRepository {

    /**
     * Persists a new technology stack entry record.
     *
     * @param appTechnology the domain model to persist
     * @return the persisted domain model, including its generated identifier
     */
    AppTechnology create(AppTechnology appTechnology);

    /**
     * Persists changes to an existing technology stack entry record.
     *
     * @param appTechnology the domain model carrying the updated values
     * @param id            the identifier of the record being updated
     * @return the persisted domain model reflecting the applied changes
     */
    AppTechnology update(AppTechnology appTechnology, Long id);

    /**
     * Persists the logical (soft) deletion of a technology stack entry record.
     *
     * @param appTechnology the domain model, with deletion tracking fields already set
     */
    void delete(AppTechnology appTechnology);

    /**
     * Resolves a single technology stack entry record by its primary key.
     *
     * @param id the record identifier
     * @return the matching domain model, or {@code null} if not found
     */
    AppTechnology findById(Long id);

    /**
     * Fetches the technology entries assigned to a single development, filtered by {@code criteria}
     * and paged. {@code appDevelopmentId} is always applied regardless of {@code criteria}, so this
     * never returns technology records belonging to a different development module.
     *
     * @param appDevelopmentId identifier of the owning development record
     * @param criteria         optional additional filters (status, search), see {@link AppTechnologyCriteria}
     * @param pageable         pagination and sorting parameters
     * @return a page of matching {@link AppTechnology} domain models
     */
    Page<AppTechnology> findAll(Long appDevelopmentId, AppTechnologyCriteria criteria, Pageable pageable);

    /**
     * Checks whether any technology record references the given architecture layer catalog entry.
     *
     * @param layerId the layer catalog identifier to check
     * @return {@code true} if at least one technology entry references the layer, {@code false} otherwise
     */
    boolean existsByLayerId(Long layerId);

    /**
     * Checks whether any technology record references the given technology catalog entry.
     *
     * @param technologyId the technology catalog identifier to check
     * @return {@code true} if at least one technology entry references the catalog entry, {@code false} otherwise
     */
    boolean existsByTechnologyId(Long technologyId);
}
