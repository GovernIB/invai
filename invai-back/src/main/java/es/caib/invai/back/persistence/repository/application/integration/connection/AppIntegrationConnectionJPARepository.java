package es.caib.invai.back.persistence.repository.application.integration.connection;

import es.caib.invai.back.persistence.model.application.integration.connection.AppIntegrationConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations and dynamic
 * specification-based search targeting live {@link AppIntegrationConnectionEntity} records.
 *
 * @since 1.0.5
 */
public interface AppIntegrationConnectionJPARepository extends JpaRepository<AppIntegrationConnectionEntity, Long>, JpaSpecificationExecutor<AppIntegrationConnectionEntity> {
}
