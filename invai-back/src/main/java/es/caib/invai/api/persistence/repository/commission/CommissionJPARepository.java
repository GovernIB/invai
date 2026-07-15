package es.caib.invai.api.persistence.repository.commission;

import es.caib.invai.api.persistence.model.CommissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations, custom query methods,
 * and constraint validation checks targeting live {@link CommissionEntity} working group records.
 *
 * @since 1.0.0
 */
@Repository
public interface CommissionJPARepository extends JpaRepository<CommissionEntity, Long> {

    /**
     * Obtains a paginated and sorted slice of all active commission nodes that have not
     * been logically soft-deleted.
     *
     * @param pageable pagination and sorting configuration parameters
     * @return a {@link Page} encapsulating the matching active {@link CommissionEntity} records
     */
    @Query("SELECT c FROM CommissionEntity c WHERE c.deletedAt IS NULL")
    Page<CommissionEntity> findAllActive(Pageable pageable);

    /**
     * Determines whether an active commission entry matching a specific structural descriptor name already exists.
     *
     * @param name target structural property name value to verify
     * @return {@code true} if a matching active record is found, {@code false} otherwise
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Determines whether an alternative active commission matching a targeted name exists,
     * excluding a designated record reference ID. Typically utilized during update uniqueness checks.
     *
     * @param name target structural property name value to verify
     * @param id   the persistent primary reference identity to exclude from evaluation scopes
     * @return {@code true} if a conflicting record matches the given criteria, {@code false} otherwise
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}