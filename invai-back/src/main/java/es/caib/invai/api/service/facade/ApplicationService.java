package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.application.DTO.ApplicationInputDTO;
import es.caib.invai.api.interna.application.DTO.ApplicationOutputDTO;
import es.caib.invai.api.persistence.repository.application.ApplicationCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Application components.
 * Acts as the primary application service boundary exposed to external API web controllers.
 *
 * @since 1.0.0
 */
public interface ApplicationService {

    /**
     * Retrieves an application by its unique internal identifier.
     * Throws an exception if the application does not exist or is inactive.
     *
     * @param id primary key tracking index mapping the system application profile
     * @return the mapped presentation outbound DTO schema representation
     */
    ApplicationOutputDTO getById(Long id);

    /**
     * Retrieves the complete list of all applications in the inventory matching multi-dimensional parameters.
     * By default, this method omits applications that have undergone a logical delete (INACTIVE).
     *
     * @param criteria searching constraints, filters, and global full-text search parameters wrapper
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<ApplicationOutputDTO> getAll(ApplicationCriteria criteria, Pageable pageable);

    /**
     * Creates a new application record in the inventory based on the input data.
     * Performs uniqueness validations for both the code and the prefix.
     *
     * @param inputDTO The required data to register the application.
     * @return The created application along with its generated ID and audit metadata.
     */
    ApplicationOutputDTO create(ApplicationInputDTO inputDTO);

    /**
     * Updates the modifiable fields of an existing application identified by its ID.
     * Protects data integrity by preventing duplicate codes or prefixes on external records.
     *
     * @param id       The unique identifier of the application to be modified.
     * @param inputDTO The new data to apply to the record.
     * @return The updated application with refreshed technical metadata.
     */
    ApplicationOutputDTO update(Long id, ApplicationInputDTO inputDTO);

    /**
     * Performs a logical deletion of the record in the system by changing its status to inactive (INACTIVE).
     * Does not perform a physical deletion in the database, in compliance with GOIB regulatory requirements.
     *
     * @param id The unique identifier of the application to deactivate.
     */
    void delete(Long id);
}