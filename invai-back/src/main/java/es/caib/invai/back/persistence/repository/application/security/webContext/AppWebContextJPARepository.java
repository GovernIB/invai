package es.caib.invai.back.persistence.repository.application.security.webContext;

import es.caib.invai.back.persistence.model.application.security.webContext.AppWebContextEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.4
 */
public interface AppWebContextJPARepository extends JpaRepository<AppWebContextEntity, Long>, JpaSpecificationExecutor<AppWebContextEntity> {
}
