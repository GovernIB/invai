package es.caib.invai.back.persistence.repository.application.accessibility;

import es.caib.invai.back.persistence.model.application.accessibility.AppAccessibilityAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository providing basic CRUD persistence for
 * {@link AppAccessibilityAudEntity} audit trail rows.
 *
 * @since 1.0.4
 */
public interface AppAccessibilityAudJPARepository extends JpaRepository<AppAccessibilityAudEntity, Long> {
}
