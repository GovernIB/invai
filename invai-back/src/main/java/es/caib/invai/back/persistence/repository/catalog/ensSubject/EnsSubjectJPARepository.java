package es.caib.invai.back.persistence.repository.catalog.ensSubject;

import es.caib.invai.back.persistence.model.catalog.ensSubject.LkupEnsSubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing read access to the static
 * {@link LkupEnsSubjectEntity} reference dictionary.
 *
 * @since 1.0.4
 */
@Repository
public interface EnsSubjectJPARepository extends JpaRepository<LkupEnsSubjectEntity, Long> {
}
