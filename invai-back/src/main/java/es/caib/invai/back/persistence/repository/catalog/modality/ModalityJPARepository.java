package es.caib.invai.back.persistence.repository.catalog.modality;

import es.caib.invai.back.persistence.model.catalog.modality.LkupModalityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing read access to the static
 * {@link LkupModalityEntity} reference dictionary.
 *
 * @since 1.0.3
 */
@Repository
public interface ModalityJPARepository extends JpaRepository<LkupModalityEntity, Long> {
}
