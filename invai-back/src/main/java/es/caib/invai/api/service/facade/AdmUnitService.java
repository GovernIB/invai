package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.maintenance.admUnit.DTO.AdmUnitInputDTO;
import es.caib.invai.api.interna.maintenance.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.api.persistence.repository.admUnit.AdmUnitCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Administrative Units ({@code AdmUnit}).
 * Acts as the primary application service boundary exposed to external API web controllers.
 *
 * @since 1.0.1
 */
public interface AdmUnitService {

    /**
     * Resolves a single active administrative unit by its unique reference identifier index.
     *
     * @param id primary key tracking index mapping the unit
     * @return the mapped presentation outbound DTO schema representation
     * @throws RuntimeException if the record is missing or soft-deleted
     */
    AdmUnitOutputDTO getById(Long id);

    /**
     * Extracts a paginated and sorted segment container enclosing all active administrative units
     * matching the supplied dynamic search criteria.
     *
     * @param filter   dynamic search criteria used to build the query predicates
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<AdmUnitOutputDTO> getAll(AdmUnitCriteria filter, Pageable pageable);

    /**
     * Validates domain invariants and creates a new administrative unit record in the system registry.
     *
     * @param inputDTO data payload structural map specifying registration fields parameters
     * @return the newly committed unit data augmented with generated identifiers and audit footprints
     */
    AdmUnitOutputDTO create(AdmUnitInputDTO inputDTO);

    /**
     * Evaluates identity constraints and overrides data structures for an active administrative unit row.
     *
     * @param id       primary sequence tracking key of the operational element to modify
     * @param inputDTO property modifications payload detailing updated state properties variables
     * @return the freshly updated representation mapping current persistent state changes
     */
    AdmUnitOutputDTO update(Long id, AdmUnitInputDTO inputDTO);

    /**
     * Coordinates a logical soft-deletion process across target administrative units records.
     * Flushes localized timeline parameters without executing physical database drop routines,
     * preserving historical traceability compliance.
     *
     * @param id primary key reference index pinpointing the registry row targeted for deactivation
     */
    void delete(Long id);

    /**
     * Reactivates a logically soft-deleted administrative unit back to active state.
     *
     * @param id the target identifier mapping the administrative unit instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link AdmUnitOutputDTO}
     * @throws RuntimeException if the record is missing or already active
     */
    AdmUnitOutputDTO reactivate(Long id);
}