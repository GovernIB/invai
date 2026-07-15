package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.commission.DTO.CommissionInputDTO;
import es.caib.invai.api.interna.commission.DTO.CommissionOutputDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Working Commissions ({@code CsCommission}).
 * Acts as the primary application service boundary exposed to external API web controllers.
 *
 * @since 1.0.0
 */
public interface CommissionService {

    /**
     * Resolves a single active technical commission by its unique reference identifier index.
     *
     * @param id primary key tracking index mapping the commission
     * @return the mapped presentation outbound DTO schema representation
     * @throws RuntimeException if the record is missing or soft-deleted
     */
    CommissionOutputDTO getById(Long id);

    /**
     * Extracts a paginated and sorted segment container enclosing all active commission entities.
     *
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<CommissionOutputDTO> getAll(Pageable pageable);

    /**
     * Validates domain invariants and creates a new technical commission record in the system registry.
     *
     * @param inputDTO data payload structural map specifying registration fields parameters
     * @return the newly committed commission data augmented with generated identifiers and audit footprints
     */
    CommissionOutputDTO create(CommissionInputDTO inputDTO);

    /**
     * Evaluates identity constraints and overrides data structures for an active commission row.
     *
     * @param id       primary sequence tracking key of the operational element to modify
     * @param inputDTO property modifications payload detailing updated state properties variables
     * @return the freshly updated representation mapping current persistent state changes
     */
    CommissionOutputDTO update(Long id, CommissionInputDTO inputDTO);

    /**
     * Coordinates a logical soft-deletion process across target commission assets profiles.
     * Flushes localized timeline parameters without executing physical database drop routines,
     * preserving historical traceability compliance.
     *
     * @param id primary key reference index pinpointing the registry row targeted for deactivation
     */
    void delete(Long id);
}