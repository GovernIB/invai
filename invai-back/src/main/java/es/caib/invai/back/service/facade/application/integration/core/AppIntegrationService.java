package es.caib.invai.back.service.facade.application.integration.core;

import es.caib.invai.back.interna.application.integration.core.DTO.AppIntegrationInputDTO;
import es.caib.invai.back.interna.application.integration.core.DTO.AppIntegrationOutputDTO;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting the "Integracio" anchor.
 *
 * @since 1.0.5
 */
public interface AppIntegrationService {

    /**
     * Fetches the integration anchor by its own primary key.
     *
     * @param id primary key of the anchor
     * @return the matching anchor, or {@code null} if no anchor exists with this ID
     */
    AppIntegrationOutputDTO getById(Long id);

    /**
     * Creates a new integration anchor for the application referenced by {@code
     * inputDTO.getApplicationId()}.
     *
     * @param inputDTO creation payload; {@code applicationId} identifies the owning application
     * @return the newly created anchor, mapped to its outbound representation
     */
    AppIntegrationOutputDTO create(AppIntegrationInputDTO inputDTO);

    /**
     * Applies the given payload onto the integration anchor identified by {@code id}.
     *
     * @param id       primary key of the anchor to update
     * @param inputDTO payload with the new field values
     * @return the updated anchor, mapped to its outbound representation
     */
    AppIntegrationOutputDTO update(Long id, AppIntegrationInputDTO inputDTO);

    /**
     * Soft-deletes the integration anchor identified by {@code id}.
     *
     * @param id primary key of the anchor to delete
     */
    void delete(Long id);
}
