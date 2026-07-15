package es.caib.invai.api.persistence.repository.admUnit;

import es.caib.invai.api.persistence.model.AdmUnitEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations, custom query methods,
 * and constraint validation checks targeting live {@link AdmUnitEntity} records.
 *
 * @since 1.0.0
 */
@Repository
public interface AdmUnitJPARepository extends JpaRepository<AdmUnitEntity, Long> {

    /**
     * Obtains a paginated and sorted slice of all active administrative units that have not
     * been logically soft-deleted.
     *
     * @param pageable pagination and sorting configuration parameters
     * @return a {@link Page} encapsulating the matching active {@link AdmUnitEntity} records
     */
    @Query("SELECT a FROM AdmUnitEntity a WHERE a.deletedAt IS NULL")
    Page<AdmUnitEntity> findAllActive(Pageable pageable);

    /**
     * Determines whether an active administrative unit with the exact designated name already exists.
     *
     * @param name target structural property name value to verify
     * @return {@code true} if a matching non-deleted record exists, {@code false} otherwise
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Determines whether an alternative active administrative unit matching a targeted name exists,
     * excluding a designated record reference ID. Typically utilized during update validation checks.
     *
     * @param name target structural property name value to verify
     * @param id   the persistent primary reference identity to exclude from evaluation scopes
     * @return {@code true} if a conflicting record matches the given name criteria, {@code false} otherwise
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);

    /**
     * Determines whether an active administrative unit matching a corporate code value already exists.
     *
     * @param code structural alphanumeric identifier code to evaluate
     * @return {@code true} if a matching non-deleted record exists, {@code false} otherwise
     */
    boolean existsByCodeAndDeletedAtIsNull(String code);

    /**
     * Determines whether an alternative active administrative unit matching a given corporate code exists,
     * excluding a specific reference ID. Typically utilized during update validation checks.
     *
     * @param code structural alphanumeric identifier code to evaluate
     * @param id   the persistent primary reference identity to exclude from evaluation scopes
     * @return {@code true} if a conflicting record matches the given code criteria, {@code false} otherwise
     */
    boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, Long id);
}