package es.caib.invai.back.persistence.repository.maintenance.integration.externalSystem;

import es.caib.invai.back.persistence.model.maintenance.integration.externalSystem.ExternalSystemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations, custom query methods,
 * and constraint validation checks targeting live {@link ExternalSystemEntity} records.
 *
 * @since 1.0.5
 */
@Repository
public interface ExternalSystemJPARepository extends JpaRepository<ExternalSystemEntity, Long>, JpaSpecificationExecutor<ExternalSystemEntity> {

    /**
     * Checks whether an active (non-deleted) external system exists with the given name.
     *
     * @param name the external system name to check
     * @return {@code true} if a matching non-deleted external system exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (non-deleted) external system exists with the given name and a
     * different identifier.
     *
     * @param name the external system name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if a matching non-deleted external system exists with a different ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}
