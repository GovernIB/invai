package es.caib.invai.back.persistence.repository.application.development.core;

import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link AppDevelopmentEntity}.
 *
 * @since 1.0.2
 */
public interface AppDevelopmentJPARepository extends JpaRepository<AppDevelopmentEntity, Long> {

    /**
     * Resolves the single development entity linked to a given application, if any — the 1-to-1
     * relationship is enforced at the facade level, not by a database constraint here.
     *
     * @param applicationId the parent application identifier
     * @return the matching entity, or empty if none exists
     */
    Optional<AppDevelopmentEntity> findByApplicationId(Long applicationId);
}
