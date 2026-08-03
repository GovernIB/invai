package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.maintenance.role.DTO.RoleInputDTO;
import es.caib.invai.api.interna.maintenance.role.DTO.RoleOutputDTO;
import es.caib.invai.api.persistence.repository.role.RoleCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting provider Roles.
 *
 * @since 1.0.2
 */
public interface RoleService {

    RoleOutputDTO getById(Long id);

    Page<RoleOutputDTO> getAll(RoleCriteria filter, Pageable pageable);

    RoleOutputDTO create(RoleInputDTO inputDTO);

    RoleOutputDTO update(Long id, RoleInputDTO inputDTO);

    void delete(Long id);

    RoleOutputDTO reactivate(Long id);
}
