package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.dir3.Dir3ValidationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository providing CRUD access to the {@link Dir3ValidationEntity}.
 *
 * @since 1.0.5
 */
@Repository
public interface Dir3ValidationJPARepository extends JpaRepository<Dir3ValidationEntity, Long> {
}
