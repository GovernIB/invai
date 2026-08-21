package es.caib.invai.back.persistence.repository.catalog.standardAdaption;

import es.caib.invai.back.persistence.model.catalog.standardAdaption.LkupStandardAdaptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing read access to the static
 * {@link LkupStandardAdaptionEntity} reference dictionary.
 *
 * @since 1.0.3
 */
@Repository
public interface StandardAdaptionJPARepository extends JpaRepository<LkupStandardAdaptionEntity, Long> {
}
