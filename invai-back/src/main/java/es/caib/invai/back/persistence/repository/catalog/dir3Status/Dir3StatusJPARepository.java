package es.caib.invai.back.persistence.repository.catalog.dir3Status;

import es.caib.invai.back.persistence.model.catalog.dir3Status.LkupDir3StatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing read access to the static
 * {@link LkupDir3StatusEntity} reference dictionary.
 *
 * @since 1.0.5
 */
@Repository
public interface Dir3StatusJPARepository extends JpaRepository<LkupDir3StatusEntity, Long> {
}
