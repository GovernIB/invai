package es.caib.invai.back.persistence.repository.application.system_database.database;

import es.caib.invai.back.persistence.model.application.system_database.database.AppDatabaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.2
 */
public interface AppDatabaseJPARepository extends JpaRepository<AppDatabaseEntity, Long>, JpaSpecificationExecutor<AppDatabaseEntity> {

    /**
     * Finds the application-database link of the given information system database grouping and
     * database, whether it is active or logically deleted.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param databaseId            identifier of the database
     * @return the matching link, or empty if the database was never linked to the grouping
     */
    Optional<AppDatabaseEntity> findByInformationSystemDbIdAndDatabaseId(Long informationSystemDbId, Long databaseId);
}
