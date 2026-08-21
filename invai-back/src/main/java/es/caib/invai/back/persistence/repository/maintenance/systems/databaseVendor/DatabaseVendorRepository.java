package es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor;

import es.caib.invai.back.service.model.maintenance.systems.databaseVendor.DatabaseVendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound Port boundary interface declaring relational persistence mechanisms
 * for the database vendor/type catalog domain model layer.
 *
 * @since 1.0.2
 */
public interface DatabaseVendorRepository {

    /**
     * Registers a new database vendor/type catalog structure inside relational tracking systems.
     */
    DatabaseVendor create(DatabaseVendor databaseVendor);

    /**
     * Commits changes onto active domain records maps identified by a target primary sequence index.
     */
    DatabaseVendor update(DatabaseVendor databaseVendor, Long id);

    /**
     * Flags tracking profiles context records as soft-deleted inside persistence layers.
     */
    void delete(DatabaseVendor databaseVendor);

    /**
     * Resolves an active database vendor record profile matching an identification primary index.
     */
    DatabaseVendor findById(Long id);

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, filtered by dynamic search criteria.
     */
    Page<DatabaseVendor> findAll(DatabaseVendorCriteria filter, Pageable pageable);

    /**
     * Confirms uniqueness of the name across active database vendor registries.
     */
    boolean existsByName(String name);

    /**
     * Checks for duplicate name conflicts excluding a target primary reference row.
     */
    boolean existsByNameAndIdNot(String name, Long id);
}
