package es.caib.invai.back.persistence.repository.maintenance.responsible.person;

import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA interface mapping historical snapshot persistence for the {@link PersonAudEntity}.
 *
 * @since 1.0.3
 */
@Repository
public interface PersonAudJPARepository extends JpaRepository<PersonAudEntity, Long> {
}
