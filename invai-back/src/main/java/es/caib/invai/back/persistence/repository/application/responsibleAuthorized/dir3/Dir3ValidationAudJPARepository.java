package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.dir3.Dir3ValidationAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository persisting the historical audit trail for {@link Dir3ValidationAudEntity}.
 *
 * @since 1.0.5
 */
public interface Dir3ValidationAudJPARepository extends JpaRepository<Dir3ValidationAudEntity, Long> {
}
