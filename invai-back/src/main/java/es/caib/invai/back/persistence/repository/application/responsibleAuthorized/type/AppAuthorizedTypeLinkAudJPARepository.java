package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.type.AppAuthorizedTypeLinkAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository providing basic CRUD persistence for
 * {@link AppAuthorizedTypeLinkAudEntity} audit trail rows.
 *
 * @since 1.0.5
 */
public interface AppAuthorizedTypeLinkAudJPARepository extends JpaRepository<AppAuthorizedTypeLinkAudEntity, Long> {
}
