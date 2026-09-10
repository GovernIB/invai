package es.caib.invai.back.persistence.repository.application.security.core;

import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.4
 */
public interface AppSecurityJPARepository extends JpaRepository<AppSecurityEntity, Long> {

    /**
     * Resolves a paginated sequence of security anchors scoped to a single
     * parent application.
     *
     * @param applicationId mandatory parent application identifier scoping the result set
     * @return the paginated matrix of matching entities
     */
    Optional<AppSecurityEntity> findByApplicationId(Long applicationId);

}
