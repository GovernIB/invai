package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.core;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.core.AppResponsibleAuthorizedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository providing CRUD access to the {@code INV_APP_RESPONSIBLE_AUTHORIZED}
 * anchor entity.
 *
 * @since 1.0.3
 */
@Repository
public interface AppResponsibleAuthorizedJPARepository extends JpaRepository<AppResponsibleAuthorizedEntity, Long> {

    /**
     * Resolves the anchor entity scoped to a single parent application.
     *
     * @param applicationId mandatory parent application identifier
     * @return the matching anchor entity, if any
     */
    Optional<AppResponsibleAuthorizedEntity> findByApplicationId(Long applicationId);
}
