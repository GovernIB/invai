package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.application.system_database.core.DTO.AppInformationSystemDbInputDTO;
import es.caib.invai.api.interna.application.system_database.core.DTO.AppInformationSystemDbOutputDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations.
 *
 * @since 1.0.2
 */
public interface AppInformationSystemDbService {

    /**
     * Retrieves a paginated sequence of information system database groupings scoped to a single
     * parent application.
     *
     * @return a paginated payload containing corresponding transfer representations
     */
    AppInformationSystemDbOutputDTO getById(Long id);

    AppInformationSystemDbOutputDTO create(AppInformationSystemDbInputDTO inputDTO);

    AppInformationSystemDbOutputDTO update(Long id, AppInformationSystemDbInputDTO inputDTO);

    void delete(Long id);
}
