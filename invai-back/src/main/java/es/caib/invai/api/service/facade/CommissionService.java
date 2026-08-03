package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.maintenance.commission.DTO.CommissionInputDTO;
import es.caib.invai.api.interna.maintenance.commission.DTO.CommissionOutputDTO;
import es.caib.invai.api.persistence.repository.commission.CommissionCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import es.caib.invai.api.exception.BusinessRuleException;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Working Commissions ({@code CsCommission}).
 * Acts as the primary application service boundary exposed to external API web controllers.
 *
 * @author invai-team
 * @since 1.0.1
 */
public interface CommissionService {

    /**
     * Resolves a single active technical commission by its unique reference identifier index.
     *
     * @param id primary key tracking index mapping the commission
     * @return the mapped presentation outbound DTO schema representation
     * @throws BusinessRuleException if the record is missing or soft-deleted
     */
    CommissionOutputDTO getById(Long id);

    /**
     * Extracts a paginated and sorted segment container enclosing all active commission entities.
     *
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<CommissionOutputDTO> getAll(CommissionCriteria criteria, Pageable pageable);

    /**
     * Validates domain invariants and creates a new technical commission record in the system registry.
     * <p>
     * Enforces unique constraints checking that neither the descriptive name nor the
     * official corporate expedient tracking number collides with an active registry row.
     * </p>
     *
     * @param inputDTO data payload structural map specifying registration fields parameters
     * @return the newly committed commission data augmented with generated identifiers and audit footprints
     * @throws BusinessRuleException if name or expedient identities violate uniqueness constraints
     */
    CommissionOutputDTO create(CommissionInputDTO inputDTO);

    /**
     * Evaluates identity constraints and overrides data structures for an active commission row.
     * <p>
     * Assures delta modifications do not trigger index collisions against separate active
     * expedient dossier codes or corporate regional names.
     * </p>
     *
     * @param id       primary sequence tracking key of the operational element to modify
     * @param inputDTO property modifications payload detailing updated state properties variables
     * @return the freshly updated representation mapping current persistent state changes
     * @throws BusinessRuleException if records are missing or conflict with tracking parameters of other rows
     */
    CommissionOutputDTO update(Long id, CommissionInputDTO inputDTO);

    /**
     * Coordinates a logical soft-deletion process across target commission assets profiles.
     * Flushes localized timeline parameters without executing physical database drop routines,
     * preserving historical traceability compliance.
     *
     * @param id primary key reference index pinpointing the registry row targeted for deactivation
     * @throws BusinessRuleException if the target reference is not found
     */
    void delete(Long id);

    /**
     * Reactivates a logically soft-deleted commission back to active state.
     *
     * @param id the target identifier mapping the commission instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link CommissionOutputDTO}
     * @throws BusinessRuleException if the record is missing or already active
     */
    CommissionOutputDTO reactivate(Long id);
}