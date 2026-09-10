package es.caib.invai.back.persistence.repository.maintenance.general.systemType;

import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations, custom query methods,
 * and constraint validation checks targeting live {@link SystemTypeEntity} structural classification records.
 *
 * @since 1.0.1
 */
@Repository
public interface SystemTypeJPARepository extends JpaRepository<SystemTypeEntity, Long>, JpaSpecificationExecutor<SystemTypeEntity> {

    /**
     * Determines whether an active system type entry matching a specific descriptor name already exists.
     *
     * @param name target structural property name value to verify
     * @return {@code true} if a matching active record is found, {@code false} otherwise
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Determines whether an alternative active system type matching a targeted name exists,
     * excluding a designated record reference ID. Typically utilized during update uniqueness checks.
     *
     * @param name target structural property name value to verify
     * @param id   the persistent primary reference identity to exclude from evaluation scopes
     * @return {@code true} if a conflicting record matches the given criteria, {@code false} otherwise
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);

    /**
     * Determines whether an active SystemType entry matching a specific Spanish name already exists.
     *
     * @param nameEs target Spanish name value to verify
     * @return {@code true} if a matching active record is found, {@code false} otherwise
     */
    boolean existsByNameEsAndDeletedAtIsNull(String nameEs);

    /**
     * Determines whether an alternative active SystemType matching a targeted Spanish name exists,
     * excluding a designated record reference ID. Typically utilized during update uniqueness checks.
     *
     * @param nameEs target Spanish name value to verify
     * @param id     the persistent primary reference identity to exclude from evaluation scopes
     * @return {@code true} if a conflicting record matches the given criteria, {@code false} otherwise
     */
    boolean existsByNameEsAndIdNotAndDeletedAtIsNull(String nameEs, Long id);
}