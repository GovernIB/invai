package es.caib.invai.back.persistence.repository.application.system_database.system;

import es.caib.invai.back.persistence.model.application.system_database.system.AppSystemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.2
 */
@Repository
public interface AppSystemJPARepository extends JpaRepository<AppSystemEntity, Long>, JpaSpecificationExecutor<AppSystemEntity> {

    /**
     * Resolves an application-system link entity by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return an {@link Optional} containing the matching entity, if found
     */
    Optional<AppSystemEntity> findById(Long id);

    /**
     * Finds the application-system link of the given information system database grouping and
     * system, whether it is active or logically deleted.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param systemId               identifier of the system
     * @return the matching link, or empty if the system was never linked to the grouping
     */
    Optional<AppSystemEntity> findByInformationSystemDbIdAndSystemId(Long informationSystemDbId, Long systemId);
}
