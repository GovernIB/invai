package es.caib.invai.back.persistence.repository.maintenance.security.personalDataProcessing;

import es.caib.invai.back.persistence.model.maintenance.security.personalDataProcessing.PersonalDataProcessingAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link PersonalDataProcessingAudEntity} snapshots.
 *
 * @since 1.0.4
 */
@Repository
public interface PersonalDataProcessingAudJPARepository extends JpaRepository<PersonalDataProcessingAudEntity, Long> {
}
