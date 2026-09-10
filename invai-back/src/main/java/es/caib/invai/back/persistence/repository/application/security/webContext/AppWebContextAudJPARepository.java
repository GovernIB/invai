package es.caib.invai.back.persistence.repository.application.security.webContext;

import es.caib.invai.back.persistence.model.application.security.webContext.AppWebContextAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository providing basic CRUD persistence for
 * {@link AppWebContextAudEntity} audit trail rows.
 *
 * @since 1.0.4
 */
public interface AppWebContextAudJPARepository extends JpaRepository<AppWebContextAudEntity, Long> {
}
