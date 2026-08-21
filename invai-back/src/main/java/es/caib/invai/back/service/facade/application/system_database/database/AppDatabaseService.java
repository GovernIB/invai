package es.caib.invai.back.service.facade.application.system_database.database;

import es.caib.invai.back.interna.application.system_database.database.DTO.AppDatabaseInputDTO;
import es.caib.invai.back.interna.application.system_database.database.DTO.AppDatabaseOutputDTO;
import es.caib.invai.back.persistence.repository.application.system_database.database.AppDatabaseCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations.
 *
 * @since 1.0.2
 */
public interface AppDatabaseService {

    /**
     * Retrieves a paginated sequence of database links scoped to a single parent application.
     *
     * @param informationSystemDbId mandatory parent application identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    Page<AppDatabaseOutputDTO> getAll(Long informationSystemDbId, AppDatabaseCriteria criteria, Pageable pageable);

    /**
     * Creates a new application-database link from the given input payload.
     *
     * @param inputDTO the creation payload
     * @return the created link, mapped to its output transfer representation
     */
    AppDatabaseOutputDTO create(AppDatabaseInputDTO inputDTO);

    /**
     * Updates the application-database link identified by {@code id} with the given
     * input payload.
     *
     * @param id       identifier of the link to update
     * @param inputDTO the update payload
     * @return the updated link, mapped to its output transfer representation
     */
    AppDatabaseOutputDTO update(Long id, AppDatabaseInputDTO inputDTO);

    /**
     * Deletes (logically) the application-database link identified by {@code id}.
     *
     * @param id identifier of the link to delete
     */
    void delete(Long id);
}
