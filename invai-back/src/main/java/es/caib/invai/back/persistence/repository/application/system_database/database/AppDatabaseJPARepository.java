package es.caib.invai.back.persistence.repository.application.system_database.database;

import es.caib.invai.back.persistence.model.application.system_database.database.AppDatabaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.2
 */
public interface AppDatabaseJPARepository extends JpaRepository<AppDatabaseEntity, Long>, JpaSpecificationExecutor<AppDatabaseEntity> {

    /**
     * Checks whether an application-database link already exists for the given
     * information system database grouping and database.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param databaseId            identifier of the database
     * @return {@code true} if a matching link exists, {@code false} otherwise
     */
    boolean existsByInformationSystemDbIdAndDatabaseId(Long informationSystemDbId, Long databaseId);

    /**
     * Checks whether an application-database link already exists for the given
     * information system database grouping and database, excluding a specific record.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param databaseId            identifier of the database
     * @param excludeId             identifier of the record to exclude from the check
     * @return {@code true} if a matching link exists, {@code false} otherwise
     */
    boolean existsByInformationSystemDbIdAndDatabaseIdAndIdNot(Long informationSystemDbId, Long databaseId, Long excludeId);
}
