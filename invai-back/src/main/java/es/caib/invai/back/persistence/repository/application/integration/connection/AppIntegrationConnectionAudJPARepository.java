package es.caib.invai.back.persistence.repository.application.integration.connection;

import es.caib.invai.back.persistence.model.application.integration.connection.AppIntegrationConnectionAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link AppIntegrationConnectionAudEntity} snapshots.
 *
 * @since 1.0.5
 */
public interface AppIntegrationConnectionAudJPARepository extends JpaRepository<AppIntegrationConnectionAudEntity, Long> {
}
