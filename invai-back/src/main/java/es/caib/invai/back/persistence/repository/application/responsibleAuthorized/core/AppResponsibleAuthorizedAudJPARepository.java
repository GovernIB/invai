package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.core;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.core.AppResponsibleAuthorizedAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository providing write access to the historical audit trail records
 * captured in the {@code INV_APP_RESPONSIBLE_AUTHORIZED_AUD} table.
 *
 * @since 1.0.3
 */
@Repository
public interface AppResponsibleAuthorizedAudJPARepository extends JpaRepository<AppResponsibleAuthorizedAudEntity, Long> {
}
