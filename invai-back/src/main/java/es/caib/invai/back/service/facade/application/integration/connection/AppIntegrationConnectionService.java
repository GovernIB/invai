package es.caib.invai.back.service.facade.application.integration.connection;

import es.caib.invai.back.interna.application.integration.connection.DTO.AppIntegrationConnectionInputDTO;
import es.caib.invai.back.interna.application.integration.connection.DTO.AppIntegrationConnectionOutputDTO;
import es.caib.invai.back.persistence.repository.application.integration.connection.AppIntegrationConnectionCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting integration connections.
 *
 * @since 1.0.5
 */
public interface AppIntegrationConnectionService {
    /**
     * Retrieves a paginated list of integration connections scoped to a single integration anchor,
     * each with its required roles and live warning status resolved.
     *
     * @param appIntegrationId mandatory parent anchor identifier scoping the result set
     * @param criteria         additional filter criteria
     * @param pageable         pagination and sorting information
     * @return a page of matching connections as response DTOs
     */
    Page<AppIntegrationConnectionOutputDTO> getAll(Long appIntegrationId, AppIntegrationConnectionCriteria criteria, Pageable pageable);

    /**
     * Creates a new integration connection.
     *
     * @param inputDTO the connection data to create
     * @return the created connection as a response DTO
     */
    AppIntegrationConnectionOutputDTO create(AppIntegrationConnectionInputDTO inputDTO);

    /**
     * Updates an existing integration connection with the given input data.
     *
     * @param id       the identifier of the connection to update
     * @param inputDTO the new connection data
     * @return the updated connection as a response DTO
     */
    AppIntegrationConnectionOutputDTO update(Long id, AppIntegrationConnectionInputDTO inputDTO);

    /**
     * Logically deletes an integration connection by its identifier.
     *
     * @param id the identifier of the connection to delete
     */
    void delete(Long id);
}
