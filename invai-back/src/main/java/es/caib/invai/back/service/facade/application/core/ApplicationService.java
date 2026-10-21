package es.caib.invai.back.service.facade.application.core;

import es.caib.invai.back.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationDetailOutputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Application components.
 * Acts as the primary application service boundary exposed to external API web controllers.
 *
 * @since 1.0.1
 */
public interface ApplicationService {

    /**
     * Retrieves an application by its unique internal identifier.
     * Throws an exception if the application does not exist or is inactive.
     *
     * @param id primary key tracking index mapping the system application profile
     * @return the mapped presentation outbound DTO schema representation
     */
    ApplicationDetailOutputDTO getById(Long id);

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

    /**
     * Reactivates a previously soft-deleted application, restoring its active status
     * and clearing deactivation metadata across its linked records.
     *
     * @param id the unique identifier of the application to reactivate
     * @return the reactivated application with refreshed technical metadata
     */
    ApplicationOutputDTO reactivate(Long id);

    /**
     * Lists the identifiers of every application in the inventory, in a single read-only
     * transaction. Lets long-running jobs process the applications one by one, each in its own
     * transaction, without holding every record in memory.
     *
     * @return the identifiers of all applications
     */
    List<Long> getAllApplicationIds();

    /**
     * Assigns to an already created application the roles Soffid has for it: those whose name starts
     * with the application's prefix followed by an underscore (case-sensitive, so prefix {@code AP2}
     * matches {@code AP2_ADMIN} but not {@code ap2_admin} nor {@code XAP2_ADMIN}). Each is assigned
     * to the application's security anchor, and its data - as Soffid reports it now - is stored in
     * {@code INV_SECURITY_ROLE}. It is idempotent: a role the application already has an active
     * assignment to (matched by its Soffid id) is skipped, so calling it again only adds the roles
     * Soffid has gained since. Roles are never removed. A Soffid failure is not swallowed: it
     * propagates and nothing is assigned.
     *
     * @param id the unique identifier of the application whose prefix names the roles to assign
     * @return the role assignments created by this call, empty if there was nothing new to assign
     * @throws es.caib.invai.back.exception.BusinessRuleException if no application exists with the given
     * ID, it is deactivated, it has no security anchor, or Soffid can't be queried
     * @throws es.caib.invai.back.exception.SoffidTimeoutException if Soffid takes too long to answer
     * @since 1.0.6
     */
    List<AppRoleOutputDTO> assignSoffidRoles(Long id);
}
