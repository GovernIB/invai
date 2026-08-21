package es.caib.invai.back.persistence.repository.application.development.provider;

import es.caib.invai.back.service.model.application.development.provider.AppProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound port boundary interface declaring relational persistence
 * mechanisms for the {@link AppProvider} entity.
 *
 * @since 1.0.2
 */
public interface AppProviderRepository {

    /**
     * Persists a new provider record.
     *
     * @param appProvider the domain model to persist
     * @return the persisted domain model, including its generated identifier
     */
    AppProvider create(AppProvider appProvider);

    /**
     * Persists changes to an existing provider record.
     *
     * @param appProvider the domain model carrying the updated values
     * @param id          the identifier of the record being updated
     * @return the persisted domain model reflecting the applied changes
     */
    AppProvider update(AppProvider appProvider, Long id);

    /**
     * Persists the logical (soft) deletion of a provider record.
     *
     * @param appProvider the domain model, with deletion tracking fields already set
     */
    void delete(AppProvider appProvider);

    /**
     * Resolves a single provider record by its primary key.
     *
     * @param id the record identifier
     * @return the matching domain model, or {@code null} if not found
     */
    AppProvider findById(Long id);

    /**
     * Resolves a paginated, criteria-filtered sequence of provider records scoped to a single
     * parent development module.
     *
     * @param appDevelopmentId mandatory parent development identifier scoping the result set
     * @param criteria         the multi-parameter business query filter boundaries
     * @param pageable         pagination structural constraints
     * @return a paginated matrix of matching domain models
     */
    Page<AppProvider> findAll(Long appDevelopmentId, AppProviderCriteria criteria, Pageable pageable);

    /**
     * Checks whether any provider record references the given role catalog entry.
     *
     * @param roleId the role catalog identifier to check
     * @return {@code true} if at least one provider references the role, {@code false} otherwise
     */
    boolean existsByRoleId(Long roleId);
}
