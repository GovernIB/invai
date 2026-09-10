package es.caib.invai.back.persistence.repository.maintenance.general.field;

import es.caib.invai.back.persistence.model.maintenance.general.field.FieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations, custom query methods,
 * and constraint validation checks targeting live {@link FieldEntity} functional area records.
 *
 * @since 1.0.1
 */
@Repository
public interface FieldJPARepository extends JpaRepository<FieldEntity, Long>, JpaSpecificationExecutor<FieldEntity> {

    /**
     * Determines whether an active business field entry matching a specific descriptor name already exists.
     *
     * @param name target structural property name value to verify
     * @return {@code true} if a matching active record is found, {@code false} otherwise
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Determines whether an alternative active functional area matching a targeted name exists,
     * excluding a designated record reference ID. Typically utilized during update uniqueness checks.
     *
     * @param name target structural property name value to verify
     * @param id   the persistent primary reference identity to exclude from evaluation scopes
     * @return {@code true} if a conflicting record matches the given criteria, {@code false} otherwise
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);

    /**
     * Determines whether an active Field entry matching a specific Spanish name already exists.
     *
     * @param nameEs target Spanish name value to verify
     * @return {@code true} if a matching active record is found, {@code false} otherwise
     */
    boolean existsByNameEsAndDeletedAtIsNull(String nameEs);

    /**
     * Determines whether an alternative active Field matching a targeted Spanish name exists,
     * excluding a designated record reference ID. Typically utilized during update uniqueness checks.
     *
     * @param nameEs target Spanish name value to verify
     * @param id     the persistent primary reference identity to exclude from evaluation scopes
     * @return {@code true} if a conflicting record matches the given criteria, {@code false} otherwise
     */
    boolean existsByNameEsAndIdNotAndDeletedAtIsNull(String nameEs, Long id);
}