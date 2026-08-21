package es.caib.invai.back.service.facade.maintenance.systems.database;

import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseInputDTO;
import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.database.DatabaseCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Database components.
 * Acts as the primary database service boundary exposed to external API web controllers.
 *
 * @since 1.0.2
 */
public interface DatabaseService {

    /**
     * Retrieves a database profile by its unique internal identifier.
     * Throws an exception if the database does not exist or is inactive.
     *
     * @param id primary key tracking index mapping the system database profile
     * @return the mapped presentation outbound DTO schema representation
     */
    DatabaseOutputDTO getById(Long id);

    /**
     * Retrieves the complete list of all databases in the inventory.
     * By default, this method omits databases that have undergone a logical delete.
     *
     * @param filter   dynamic search criteria and full-text keyword filters
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<DatabaseOutputDTO> getAll(DatabaseCriteria filter, Pageable pageable);

    /**
     * Creates a new database record in the inventory based on the input data.
     * Performs uniqueness validations for server host and service instances.
     *
     * @param inputDTO The required data to register the database.
     * @return The created database along with its generated ID and audit metadata.
     */
    DatabaseOutputDTO create(DatabaseInputDTO inputDTO);

    /**
     * Updates the modifiable fields of an existing database identified by its ID.
     * Protects data integrity by preventing duplicate configurations on external records.
     *
     * @param id       The unique identifier of the database to be modified.
     * @param inputDTO The new data to apply to the record.
     * @return The updated database with refreshed technical metadata.
     */
    DatabaseOutputDTO update(Long id, DatabaseInputDTO inputDTO);

    /**
     * Performs a logical deletion of the record in the system by flagging timelines.
     * Does not perform a physical deletion in the database, in compliance with GOIB regulatory requirements.
     *
     * @param id The unique identifier of the database to deactivate.
     */
    void delete(Long id);

    /**
     * Reactivates a logically soft-deleted database back to active state.
     *
     * @param id the target identifier mapping the database instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link DatabaseOutputDTO}
     */
    DatabaseOutputDTO reactivate(Long id);
}