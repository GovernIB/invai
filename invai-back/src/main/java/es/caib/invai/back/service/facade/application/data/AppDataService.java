package es.caib.invai.back.service.facade.application.data;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.data.DTO.AppDataInputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataOutputDTO;

/**
 * Facade boundary declaring the transactional use cases for managing the "Open Data" tab anchor,
 * whose {@code getById} additionally resolves the owning application's published GET endpoints
 * live from its own external REST API on every call.
 *
 * @since 1.0.5
 */
public interface AppDataService {

    /**
     * Fetches the open data anchor by its own primary key, together with every GET endpoint
     * currently published by the owning application's own external REST API (resolved live - see
     * {@code es.caib.invai.back.rest.openapi.OpenApiClient}).
     *
     * @param id primary key of the open data anchor
     * @return the matching anchor, or {@code null} if no anchor exists with this ID
     * @throws BusinessRuleException if the anchor exists but its live OpenAPI document can't be
     * fetched or parsed
     */
    AppDataOutputDTO getById(Long id);

    /**
     * Creates a new open data anchor for the application referenced by {@code
     * inputDTO.getApplicationId()}.
     *
     * @param inputDTO creation payload; {@code applicationId} identifies the owning application
     * @return the newly created anchor, mapped to its outbound representation
     */
    AppDataOutputDTO create(AppDataInputDTO inputDTO);

    /**
     * Applies the given payload onto the open data anchor identified by {@code id}.
     *
     * @param id       primary key of the open data anchor to update
     * @param inputDTO payload with the new field values
     * @return the updated anchor, mapped to its outbound representation
     * @throws BusinessRuleException if no anchor exists with the given ID
     */
    AppDataOutputDTO update(Long id, AppDataInputDTO inputDTO);

    /**
     * Soft-deletes the open data anchor identified by {@code id}.
     *
     * @param id primary key of the open data anchor to delete
     * @throws BusinessRuleException if no anchor exists with the given ID, or it is already soft-deleted
     */
    void delete(Long id);
}
