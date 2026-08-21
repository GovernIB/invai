package es.caib.invai.back.persistence.repository.maintenance.systems.database;

import es.caib.invai.back.persistence.model.maintenance.systems.database.DatabaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Infrastructure Interface providing out-of-the-box operations
 * for the {@link DatabaseEntity} database entity.
 *
 * @since 1.0.2
 */
@Repository
public interface DatabaseJPARepository extends JpaRepository<DatabaseEntity, Long>, JpaSpecificationExecutor<DatabaseEntity> {

    /**
     * Checks whether a database entry already exists for the given host server and service name.
     *
     * @param serverId the host server identifier
     * @param service  the database service schema identifier
     * @return {@code true} if a matching record exists, {@code false} otherwise
     */
    boolean existsByServerIdAndService(Long serverId, String service);

    /**
     * Checks whether a database entry with the given host server and service name exists,
     * excluding the record identified by {@code id}.
     *
     * @param serverId the host server identifier
     * @param service  the database service schema identifier
     * @param id       the identifier of the record to exclude from the check
     * @return {@code true} if a conflicting record exists, {@code false} otherwise
     */
    boolean existsByServerIdAndServiceAndIdNot(Long serverId, String service, Long id);
}