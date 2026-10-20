package es.caib.invai.back.persistence.repository.application.integration.core;

import es.caib.invai.back.persistence.model.application.integration.core.AppIntegrationAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link AppIntegrationAudEntity} snapshots.
 *
 * @since 1.0.5
 */
public interface AppIntegrationAudJPARepository extends JpaRepository<AppIntegrationAudEntity, Long> {
}
