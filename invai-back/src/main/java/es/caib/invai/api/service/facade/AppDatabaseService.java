package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.application.system_database.database.DTO.AppDatabaseInputDTO;
import es.caib.invai.api.interna.application.system_database.database.DTO.AppDatabaseOutputDTO;
import es.caib.invai.api.persistence.repository.application.system_database.database.AppDatabaseCriteria;
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

    AppDatabaseOutputDTO create(AppDatabaseInputDTO inputDTO);

    AppDatabaseOutputDTO update(Long id, AppDatabaseInputDTO inputDTO);

    void delete(Long id);
}
