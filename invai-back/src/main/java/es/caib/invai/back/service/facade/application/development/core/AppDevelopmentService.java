package es.caib.invai.back.service.facade.application.development.core;

import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentInputDTO;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentOutputDTO;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations
 * targeting the main application development module detail.
 *
 * @since 1.0.2
 */
public interface AppDevelopmentService {

    /**
     * Retrieves a paginated sequence of development records scoped to a single parent application.
     *
     * @param id mandatory parent application identifier scoping the result set
     * @return a paginated payload containing corresponding transfer representations
     */
    DevelopmentOutputDTO getById(Long id);

    /**
     * Registers a new development module detail record.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created entity
     */
    DevelopmentOutputDTO create(DevelopmentInputDTO inputDTO);

    /**
     * Updates an active development record with modified metadata parameters.
     *
     * @param id       primary corporate tracking reference key
     * @param inputDTO mutated parameter dataset structures
     * @return updated transfer data mapping payload state
     */
    DevelopmentOutputDTO update(Long id, DevelopmentInputDTO inputDTO);

    /**
     * Transitions a target development record into an inactive state by enforcing logical deletion structures.
     *
     * @param id target primary structural key to process for deprecation
     */
    void delete(Long id);
}
