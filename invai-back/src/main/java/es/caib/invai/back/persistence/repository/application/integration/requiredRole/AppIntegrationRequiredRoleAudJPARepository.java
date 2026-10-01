package es.caib.invai.back.persistence.repository.application.integration.requiredRole;

import es.caib.invai.back.persistence.model.application.integration.requiredRole.AppIntegrationRequiredRoleAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link AppIntegrationRequiredRoleAudEntity} snapshots.
 *
 * @since 1.0.5
 */
public interface AppIntegrationRequiredRoleAudJPARepository extends JpaRepository<AppIntegrationRequiredRoleAudEntity, Long> {
}
