package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.type.AppAuthorizedTypeLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository providing CRUD access to the {@link AppAuthorizedTypeLinkEntity} join.
 *
 * @since 1.0.3
 */
public interface AppAuthorizedTypeLinkJPARepository extends JpaRepository<AppAuthorizedTypeLinkEntity, Long> {

    /**
     * Resolves all authorization type join rows attached to a given authorized person anchor.
     *
     * @param appAuthorizedId the authorized person anchor identifier
     * @return the matching join rows
     */
    List<AppAuthorizedTypeLinkEntity> findAllByAppAuthorizedId(Long appAuthorizedId);
}
