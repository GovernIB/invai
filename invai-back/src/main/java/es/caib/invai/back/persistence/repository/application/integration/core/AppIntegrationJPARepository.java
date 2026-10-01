package es.caib.invai.back.persistence.repository.application.integration.core;

import es.caib.invai.back.persistence.model.application.integration.core.AppIntegrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository providing CRUD access to {@link AppIntegrationEntity} rows, plus the
 * by-application lookup used to fetch the anchor backing an application's "Integracio" tab.
 *
 * @since 1.0.5
 */
public interface AppIntegrationJPARepository extends JpaRepository<AppIntegrationEntity, Long> {

    /**
     * Looks up the integration anchor for the given application, regardless of soft-delete status.
     *
     * @param applicationId identifier of the owning application
     * @return the matching row, if any (present even if soft-deleted)
     */
    Optional<AppIntegrationEntity> findByApplicationId(Long applicationId);
}
