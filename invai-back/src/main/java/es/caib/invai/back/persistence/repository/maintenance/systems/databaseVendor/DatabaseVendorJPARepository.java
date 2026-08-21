package es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor;

import es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor.DatabaseVendorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Native Spring Data JPA repository layer providing entity lifecycle updates, custom queries,
 * and constraint validations for {@link DatabaseVendorEntity}.
 *
 * @since 1.0.2
 */
@Repository
public interface DatabaseVendorJPARepository extends JpaRepository<DatabaseVendorEntity, Long>, JpaSpecificationExecutor<DatabaseVendorEntity> {

    /**
     * Resolves a database vendor entity by its unique identifier.
     *
     * @param id the entity identifier
     * @return an {@link Optional} containing the matching entity, or empty if not found
     */
    Optional<DatabaseVendorEntity> findById(Long id);

    /**
     * Checks whether an active (non soft-deleted) database vendor with the given name exists.
     *
     * @param name the vendor/type name to check
     * @return {@code true} if a matching active record exists, {@code false} otherwise
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (non soft-deleted) database vendor with the given name exists,
     * excluding the record identified by {@code id}.
     *
     * @param name the vendor/type name to check
     * @param id   the identifier of the record to exclude from the check
     * @return {@code true} if a conflicting active record exists, {@code false} otherwise
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}
