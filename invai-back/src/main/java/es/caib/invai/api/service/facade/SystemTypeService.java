package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.systemType.DTO.SystemTypeInputDTO;
import es.caib.invai.api.interna.systemType.DTO.SystemTypeOutputDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting System Infrastructure Classifications ({@code SystemType}).
 * Acts as the primary application service boundary exposed to external API web controllers.
 *
 * @since 1.0.0
 */
public interface SystemTypeService {

    /**
     * Resolves a single active infrastructure system type by its unique reference identifier index.
     *
     * @param id primary key tracking index mapping the architecture type
     * @return the mapped presentation outbound DTO schema representation
     * @throws RuntimeException if the record is missing or soft-deleted
     */
    SystemTypeOutputDTO getById(Long id);

    /**
     * Extracts a paginated and sorted segment container enclosing all active core system types.
     *
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<SystemTypeOutputDTO> getAll(Pageable pageable);

    /**
     * Validates domain invariants and creates a new system type configuration record in the system registry.
     *
     * @param inputDTO data payload structural map specifying registration fields parameters
     * @return the newly committed system type data augmented with generated identifiers and audit footprints
     */
    SystemTypeOutputDTO create(SystemTypeInputDTO inputDTO);

    /**
     * Evaluates identity constraints and overrides data structures for an active architectural system type row.
     *
     * @param id       primary sequence tracking key of the operational element to modify
     * @param inputDTO property modifications payload detailing updated state properties variables
     * @return the freshly updated representation mapping current persistent state changes
     */
    SystemTypeOutputDTO update(Long id, SystemTypeInputDTO inputDTO);

    /**
     * Coordinates a logical soft-deletion process across target system type nodes.
     * Flushes localized timeline parameters without executing physical database drop routines,
     * preserving historical traceability compliance.
     *
     * @param id primary key reference index pinpointing the registry row targeted for deactivation
     */
    void delete(Long id);
}