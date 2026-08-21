package es.caib.invai.back.service.facade.maintenance.general.category;

import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryInputDTO;
import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.general.category.CategoryCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Taxonomy Categories ({@code Category}).
 * Acts as the primary application service boundary exposed to external API web controllers.
 *
 * @since 1.0.1
 */
public interface CategoryService {

    /**
     * Resolves a single active taxonomy category by its unique reference identifier index.
     *
     * @param id primary key tracking index mapping the category
     * @return the mapped presentation outbound DTO schema representation
     * @throws RuntimeException if the record is missing or soft-deleted
     */
    CategoryOutputDTO getById(Long id);

    /**
     * Extracts a paginated and sorted segment container enclosing all active asset taxonomy categories.
     *
     * @param filter   dynamic search criteria used to build the query specification
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<CategoryOutputDTO> getAll(CategoryCriteria filter, Pageable pageable);

    /**
     * Validates domain invariants and creates a new taxonomy category record in the system registry.
     * Performs uniqueness evaluations to guard against structural label naming collisions.
     *
     * @param inputDTO data payload structural map specifying registration fields parameters
     * @return the newly committed category data augmented with generated identifiers and audit footprints
     */
    CategoryOutputDTO create(CategoryInputDTO inputDTO);

    /**
     * Evaluates identity constraints and overrides data structures for an active taxonomy category row.
     * Protects database integrity by preventing duplicate nomenclature collisions across external records.
     *
     * @param id       primary sequence tracking key of the operational element to modify
     * @param inputDTO property modifications payload detailing updated state properties variables
     * @return the freshly updated representation mapping current persistent state changes
     */
    CategoryOutputDTO update(Long id, CategoryInputDTO inputDTO);

    /**
     * Coordinates a logical soft-deletion process across target categorization profiles.
     * Flushes localized timeline parameters without executing physical database drop routines,
     * preserving historical traceability compliance in accordance with regulatory mandates.
     *
     * @param id primary key reference index pinpointing the registry row targeted for deactivation
     */
    void delete(Long id);

    /**
     * Reactivates a logically soft-deleted taxonomy category back to active state.
     *
     * @param id the target identifier mapping the category instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link CategoryOutputDTO}
     * @throws RuntimeException if the record is missing or already active
     */
    CategoryOutputDTO reactivate(Long id);
}