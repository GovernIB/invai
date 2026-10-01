package es.caib.invai.back.persistence.repository.application.integration.connection;

import es.caib.invai.back.service.model.application.integration.connection.AppIntegrationConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port abstracting CRUD and paginated search operations for
 * {@link AppIntegrationConnection} aggregates.
 *
 * @since 1.0.5
 */
public interface AppIntegrationConnectionRepository {

    /**
     * Persists a new integration connection.
     *
     * @param appIntegrationConnection the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppIntegrationConnection create(AppIntegrationConnection appIntegrationConnection);

    /**
     * Updates the integration connection identified by {@code id}.
     *
     * @param appIntegrationConnection the model carrying the updated values
     * @param id                  identifier of the record to update
     * @return the updated model
     */
    AppIntegrationConnection update(AppIntegrationConnection appIntegrationConnection, Long id);

    /**
     * Deletes (logically) the given integration connection.
     *
     * @param appIntegrationConnection the model to delete
     */
    void delete(AppIntegrationConnection appIntegrationConnection);

    /**
     * Resolves an integration connection by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppIntegrationConnection findById(Long id);

    /**
     * Resolves a paginated, filtered list of integration connections scoped to a single parent
     * integration anchor.
     *
     * @param appIntegrationId mandatory parent anchor identifier scoping the result set
     * @param criteria         additional filter criteria
     * @param pageable         pagination and sorting instructions
     * @return the paginated page of matching models
     */
    Page<AppIntegrationConnection> findAll(Long appIntegrationId, AppIntegrationConnectionCriteria criteria, Pageable pageable);
}
