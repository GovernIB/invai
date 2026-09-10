package es.caib.invai.back.persistence.repository.application.development.provider;

import es.caib.invai.back.service.model.application.development.provider.AppProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port for the "AppProvider" list: provider (company) assignments hanging
 * many-to-one off a single development record. Implemented by
 * {@link AppProviderRepositoryAdapter}, which also writes an audit trail row alongside every
 * create/update/delete.
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
     * Fetches the providers assigned to a single development, filtered by {@code criteria} and
     * paged. {@code appDevelopmentId} is always applied regardless of {@code criteria}, so this
     * never returns records belonging to a different development module.
     *
     * @param appDevelopmentId mandatory parent development identifier scoping the result set
     * @param criteria         optional additional filters (status, search); {@code null} applies
     *                         only the development scope
     * @param pageable         pagination and sorting parameters
     * @return the matching page of domain models
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
