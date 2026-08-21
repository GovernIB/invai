package es.caib.invai.back.service.facade.application.development.provider;

import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderInputDTO;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations
 * targeting provider assignments linked to development modules.
 *
 * @since 1.0.2
 */
public interface AppProviderService {

    /**
     * Retrieves a paginated sequence of provider records scoped to a single parent development module.
     *
     * @param appDevelopmentId mandatory parent development identifier scoping the result set
     * @param criteria         the multi-parameter business query filter boundaries
     * @param pageable         pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    Page<AppProviderOutputDTO> getAll(Long appDevelopmentId, AppProviderCriteria criteria, Pageable pageable);

    /**
     * Registers a new provider assignment record.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created entity
     */
    AppProviderOutputDTO create(AppProviderInputDTO inputDTO);

    /**
     * Updates an active provider record with modified metadata parameters.
     *
     * @param id       primary corporate tracking reference key
     * @param inputDTO mutated parameter dataset structures
     * @return updated transfer data mapping payload state
     */
    AppProviderOutputDTO update(Long id, AppProviderInputDTO inputDTO);

    /**
     * Transitions a target provider record into an inactive state by enforcing logical deletion structures.
     *
     * @param id target primary structural key to process for deprecation
     */
    void delete(Long id);
}
