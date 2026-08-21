package es.caib.invai.back.persistence.repository.catalog.status;

import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing read access to the static
 * {@link LkupStatusEntity} reference dictionary.
 *
 * @since 1.0.3
 */
@Repository
public interface StatusJPARepository extends JpaRepository<LkupStatusEntity, Long> {
}
