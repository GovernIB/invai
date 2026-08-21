package es.caib.invai.back.persistence.repository.application.system_database.system;

import es.caib.invai.back.persistence.model.application.system_database.system.AppSystemAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository providing basic CRUD persistence for
 * {@link AppSystemAudEntity} audit trail rows.
 *
 * @since 1.0.2
 */
public interface AppSystemAudJPARepository extends JpaRepository<AppSystemAudEntity, Long> {
}
