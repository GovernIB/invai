package es.caib.invai.back.persistence.repository.catalog.responsibleType;

import es.caib.invai.back.persistence.model.catalog.responsibleType.LkupResponsibleTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing read access to the static
 * {@link LkupResponsibleTypeEntity} reference dictionary.
 *
 * @since 1.0.3
 */
@Repository
public interface ResponsibleTypeJPARepository extends JpaRepository<LkupResponsibleTypeEntity, Long> {
}
