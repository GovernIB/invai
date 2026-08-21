package es.caib.invai.back.service.facade.maintenance.systems.databaseVendor;

import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorInputDTO;
import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor.DatabaseVendorCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting DatabaseVendor components.
 * Acts as the primary service boundary exposed to external API web controllers.
 *
 * @since 1.0.2
 */
public interface DatabaseVendorService {

    /**
     * Retrieves a database vendor by its unique internal identifier.
     *
     * @param id primary key tracking index mapping the database vendor profile
     * @return the mapped presentation outbound DTO schema representation
     */
    DatabaseVendorOutputDTO getById(Long id);

    /**
     * Retrieves the complete list of all database vendors matching pagination thresholds.
     *
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<DatabaseVendorOutputDTO> getAll(DatabaseVendorCriteria filter, Pageable pageable);

    /**
     * Creates a new database vendor record based on the input data.
     *
     * @param inputDTO The required data to register the database vendor.
     * @return The created database vendor along with its generated ID.
     */
    DatabaseVendorOutputDTO create(DatabaseVendorInputDTO inputDTO);

    /**
     * Updates the modifiable fields of an existing database vendor identified by its ID.
     *
     * @param id       The unique identifier of the database vendor to be modified.
     * @param inputDTO The new data to apply to the record.
     * @return The updated database vendor snapshot details.
     */
    DatabaseVendorOutputDTO update(Long id, DatabaseVendorInputDTO inputDTO);

    /**
     * Performs a logical deletion of the record in the system by changing its status to inactive.
     *
     * @param id The unique identifier of the database vendor to deactivate.
     */
    void delete(Long id);

    /**
     * Reactivates a logically soft-deleted database vendor back to active state.
     *
     * @param id the target identifier mapping the database vendor instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link DatabaseVendorOutputDTO}
     */
    DatabaseVendorOutputDTO reactivate(Long id);
}
