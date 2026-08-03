package es.caib.invai.api.persistence.repository.role;

import es.caib.invai.api.service.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the provider Role layer.
 *
 * @since 1.0.2
 */
public interface RoleRepository {

    Role findById(Long id);

    Page<Role> findAll(RoleCriteria filter, Pageable pageable);

    Role create(Role role);

    Role update(Role role, Long id);

    void delete(Role role);

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}
