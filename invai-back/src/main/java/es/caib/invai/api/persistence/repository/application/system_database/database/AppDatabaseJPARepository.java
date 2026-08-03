package es.caib.invai.api.persistence.repository.application.system_database.database;

import es.caib.invai.api.persistence.model.AppDatabaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.2
 */
public interface AppDatabaseJPARepository extends JpaRepository<AppDatabaseEntity, Long>, JpaSpecificationExecutor<AppDatabaseEntity> {

    boolean existsByInformationSystemDbIdAndDatabaseId(Long informationSystemDbId, Long databaseId);

    boolean existsByInformationSystemDbIdAndDatabaseIdAndIdNot(Long informationSystemDbId, Long databaseId, Long excludeId);
}
