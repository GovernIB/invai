package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.application.development.technology.DTO.AppTechnologyInputDTO;
import es.caib.invai.api.interna.application.development.technology.DTO.AppTechnologyOutputDTO;
import es.caib.invai.api.persistence.repository.application.development.technology.AppTechnologyCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations
 * targeting technology stack entries linked to development modules.
 *
 * @since 1.0.2
 */
public interface AppTechnologyService {

    /**
     * Retrieves a paginated sequence of technology records scoped to a single parent development module.
     *
     * @param appDevelopmentId mandatory parent development identifier scoping the result set
     * @param criteria         the multi-parameter business query filter boundaries
     * @param pageable         pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    Page<AppTechnologyOutputDTO> getAll(Long appDevelopmentId, AppTechnologyCriteria criteria, Pageable pageable);

    /**
     * Registers a new technology stack entry record.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created entity
     */
    AppTechnologyOutputDTO create(AppTechnologyInputDTO inputDTO);

    /**
     * Updates an active technology record with modified metadata parameters.
     *
     * @param id       primary corporate tracking reference key
     * @param inputDTO mutated parameter dataset structures
     * @return updated transfer data mapping payload state
     */
    AppTechnologyOutputDTO update(Long id, AppTechnologyInputDTO inputDTO);

    /**
     * Transitions a target technology record into an inactive state by enforcing logical deletion structures.
     *
     * @param id target primary structural key to process for deprecation
     */
    void delete(Long id);
}
