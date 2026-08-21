package es.caib.invai.back.persistence.repository.application.system_database.core;

import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository providing basic CRUD persistence for
 * {@link AppInformationSystemDbAudEntity} audit trail rows.
 *
 * @since 1.0.2
 */
public interface AppInformationSystemDbAudJPARepository extends JpaRepository<AppInformationSystemDbAudEntity, Long> {
}
