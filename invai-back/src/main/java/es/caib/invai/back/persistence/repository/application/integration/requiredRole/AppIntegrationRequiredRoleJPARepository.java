package es.caib.invai.back.persistence.repository.application.integration.requiredRole;

import es.caib.invai.back.persistence.model.application.integration.requiredRole.AppIntegrationRequiredRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations targeting live
 * {@link AppIntegrationRequiredRoleEntity} records.
 *
 * @since 1.0.5
 */
public interface AppIntegrationRequiredRoleJPARepository extends JpaRepository<AppIntegrationRequiredRoleEntity, Long> {

    /**
     * Finds every active (non-soft-deleted) required role row for the given integration connection.
     *
     * @param appIntegrationConnectionId identifier of the parent integration connection
     * @return every matching active row
     */
    List<AppIntegrationRequiredRoleEntity> findAllByAppIntegrationConnection_IdAndDeletedAtIsNull(Long appIntegrationConnectionId);
}
