package es.caib.invai.back.persistence.repository.application.system_database.database;

import es.caib.invai.back.persistence.model.application.system_database.database.AppDatabaseAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository providing basic CRUD persistence for
 * {@link AppDatabaseAudEntity} audit trail rows.
 *
 * @since 1.0.2
 */
public interface AppDatabaseAudJPARepository extends JpaRepository<AppDatabaseAudEntity, Long> {
}
