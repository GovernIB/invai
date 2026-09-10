package es.caib.invai.back.persistence.repository.application.security.role;

import es.caib.invai.back.persistence.model.application.security.role.AppRoleAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository providing basic CRUD persistence for
 * {@link AppRoleAudEntity} audit trail rows.
 *
 * @since 1.0.4
 */
public interface AppRoleAudJPARepository extends JpaRepository<AppRoleAudEntity, Long> {
}
