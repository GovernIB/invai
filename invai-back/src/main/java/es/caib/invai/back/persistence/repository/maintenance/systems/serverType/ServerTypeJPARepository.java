package es.caib.invai.back.persistence.repository.maintenance.systems.serverType;

import es.caib.invai.back.persistence.model.maintenance.systems.serverType.LkupServerTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing read access to the static
 * {@link LkupServerTypeEntity} reference dictionary.
 *
 * @since 1.0.2
 */
@Repository
public interface ServerTypeJPARepository extends JpaRepository<LkupServerTypeEntity, Long> {
}
