package es.caib.invai.back.persistence.repository.application.development.core;

import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.2
 */
public interface AppDevelopmentJPARepository extends JpaRepository<AppDevelopmentEntity, Long> {

    /**
     * Resolves a paginated sequence of development records scoped to a single parent application.
     *
     * @param applicationId mandatory parent application identifier scoping the result set
     * @return the paginated matrix of matching entities
     */
    Optional<AppDevelopmentEntity> findByApplicationId(Long applicationId);
}
