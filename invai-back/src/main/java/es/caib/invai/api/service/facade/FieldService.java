package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.field.DTO.FieldInputDTO;
import es.caib.invai.api.interna.field.DTO.FieldOutputDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Functional Area Fields ({@code Field}).
 * Acts as the primary application service boundary exposed to external API web controllers.
 *
 * @since 1.0.0
 */
public interface FieldService {

    /**
     * Resolves a single active functional area field by its unique reference identifier index.
     *
     * @param id primary key tracking index mapping the field
     * @return the mapped presentation outbound DTO schema representation
     * @throws RuntimeException if the record is missing or soft-deleted
     */
    FieldOutputDTO getById(Long id);

    /**
     * Extracts a paginated and sorted segment container enclosing all active functional fields.
     *
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<FieldOutputDTO> getAll(Pageable pageable);

    /**
     * Validates domain invariants and creates a new functional area field record in the system registry.
     *
     * @param inputDTO data payload structural map specifying registration fields parameters
     * @return the newly committed field data augmented with generated identifiers and audit footprints
     */
    FieldOutputDTO create(FieldInputDTO inputDTO);

    /**
     * Evaluates identity constraints and overrides data structures for an active field row.
     *
     * @param id       primary sequence tracking key of the operational element to modify
     * @param inputDTO property modifications payload detailing updated state properties variables
     * @return the freshly updated representation mapping current persistent state changes
     */
    FieldOutputDTO update(Long id, FieldInputDTO inputDTO);

    /**
     * Coordinates a logical soft-deletion process across target business fields structures.
     * Flushes localized timeline parameters without executing physical database drop routines,
     * preserving historical traceability compliance.
     *
     * @param id primary key reference index pinpointing the registry row targeted for deactivation
     */
    void delete(Long id);
}