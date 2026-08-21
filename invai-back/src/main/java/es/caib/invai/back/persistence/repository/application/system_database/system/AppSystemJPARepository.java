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
     * Checks whether an application-system link already exists for the given
     * information system database grouping and system.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param systemId               identifier of the system
     * @return {@code true} if a matching link exists, {@code false} otherwise
     */
    boolean existsByInformationSystemDbIdAndSystemId(Long informationSystemDbId, Long systemId);

    /**
     * Checks whether an application-system link already exists for the given
     * information system database grouping and system, excluding a specific record.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param systemId               identifier of the system
     * @param id                     identifier of the record to exclude from the check
     * @return {@code true} if a matching link exists, {@code false} otherwise
     */
    boolean existsByInformationSystemDbIdAndSystemIdAndIdNot(Long informationSystemDbId, Long systemId, Long id);
}
