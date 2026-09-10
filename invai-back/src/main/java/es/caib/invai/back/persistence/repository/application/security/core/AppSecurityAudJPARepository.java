package es.caib.invai.back.persistence.repository.application.security.core;

import es.caib.invai.back.persistence.model.application.security.core.AppSecurityAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository providing basic CRUD persistence for
 * {@link AppSecurityAudEntity} audit trail rows.
 *
 * @since 1.0.4
 */
public interface AppSecurityAudJPARepository extends JpaRepository<AppSecurityAudEntity, Long> {
}
