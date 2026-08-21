package es.caib.invai.back.persistence.repository.application.development.technology;

import es.caib.invai.back.service.model.application.development.technology.AppTechnology;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound port boundary interface declaring relational persistence
 * mechanisms for the {@link AppTechnology} entity.
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
     * Resolves a paginated, criteria-filtered sequence of technology records scoped to a single
     * parent development module.
     *
     * @param appDevelopmentId mandatory parent development identifier scoping the result set
     * @param criteria         the multi-parameter business query filter boundaries
     * @param pageable         pagination structural constraints
     * @return a paginated matrix of matching domain models
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
