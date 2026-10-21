package es.caib.invai.back.persistence.repository.catalog.validationStatus;

import es.caib.invai.back.persistence.model.catalog.validationStatus.LkupValidationStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing read access to the static
 * {@link LkupValidationStatusEntity} reference dictionary.
 *
 * @since 1.0.5
 */
@Repository
public interface ValidationStatusJPARepository extends JpaRepository<LkupValidationStatusEntity, Long> {
}
