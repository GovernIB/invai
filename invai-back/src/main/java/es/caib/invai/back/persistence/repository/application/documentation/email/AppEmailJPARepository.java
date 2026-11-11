package es.caib.invai.back.persistence.repository.application.documentation.email;

import es.caib.invai.back.persistence.model.application.documentation.email.AppEmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing the persistence of the
 * {@link AppEmailEntity} records of the history of sent e-mails.
 *
 * @since 1.0.7
 */
@Repository
public interface AppEmailJPARepository extends JpaRepository<AppEmailEntity, Long> {
}
