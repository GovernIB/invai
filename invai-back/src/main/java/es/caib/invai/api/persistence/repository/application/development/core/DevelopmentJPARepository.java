package es.caib.invai.api.persistence.repository.application.development.core;

import es.caib.invai.api.persistence.model.DevelopmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.2
 */
public interface DevelopmentJPARepository extends JpaRepository<DevelopmentEntity, Long> {

    /**
     * Resolves a paginated sequence of development records scoped to a single parent application.
     *
     * @param applicationId mandatory parent application identifier scoping the result set
     * @return the paginated matrix of matching entities
     */
    Optional<DevelopmentEntity> findByApplicationId(Long applicationId);
}
