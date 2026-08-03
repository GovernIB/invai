package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.application.system_database.system.DTO.AppSystemInputDTO;
import es.caib.invai.api.interna.application.system_database.system.DTO.AppSystemOutputDTO;
import es.caib.invai.api.persistence.repository.application.system_database.system.AppSystemCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting AppSystem link components.
 *
 * @since 1.0.2
 */
public interface AppSystemService {

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent application. Never returns system links belonging to other applications.
     *
     * @param informationSystemDbId mandatory parent application identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination layout boundaries and sorting rules configuration
     * @return a partitioned matrix page containing mapped output definitions
     */
    Page<AppSystemOutputDTO> getAll(Long informationSystemDbId, AppSystemCriteria criteria, Pageable pageable);

    /**
     * Persists a new application system infrastructure relationship allocation configuration.
     * Evaluates active constraint descriptors before routing parameters across execution lines.
     *
     * @param inputDTO properties dataset containing link mappings and configuration variables
     * @return the newly created snapshot parameters state model
     */
    AppSystemOutputDTO create(AppSystemInputDTO inputDTO);

    /**
     * Alters active configuration profiles mapping properties corresponding to target database references.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items to be merged
     * @return current modified configuration state properties details wrapper
     */
    AppSystemOutputDTO update(Long id, AppSystemInputDTO inputDTO);

    /**
     * Routes explicit request signals targeting domain deactivation soft deletion routines.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     */
    void delete(Long id);
}
