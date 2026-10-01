package es.caib.invai.back.persistence.repository.application.integration.requiredRole;

import es.caib.invai.back.service.model.application.integration.requiredRole.AppIntegrationRequiredRole;

import java.util.List;

/**
 * Persistence port abstracting CRUD and lookup operations for {@link AppIntegrationRequiredRole}
 * aggregates. Unlike its sibling anchors, this one is never paginated on its own - a given
 * integration connection only ever has a handful of required roles, always fetched and shown nested
 * inside the owning connection's own response (see {@code AppIntegrationConnectionServiceFacadeBean}).
 *
 * @since 1.0.5
 */
public interface AppIntegrationRequiredRoleRepository {

    /**
     * Persists a new required role row.
     *
     * @param appIntegrationRequiredRole the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppIntegrationRequiredRole create(AppIntegrationRequiredRole appIntegrationRequiredRole);

    /**
     * Deletes (logically) the given required role row.
     *
     * @param appIntegrationRequiredRole the model to delete
     */
    void delete(AppIntegrationRequiredRole appIntegrationRequiredRole);

    /**
     * Resolves every active required role row belonging to the given integration connection.
     *
     * @param appIntegrationConnectionId identifier of the parent integration connection
     * @return every active required role row, in no particular guaranteed order beyond insertion
     */
    List<AppIntegrationRequiredRole> findAllActiveByAppIntegrationConnectionId(Long appIntegrationConnectionId);
}
