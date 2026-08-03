package es.caib.invai.api.persistence.repository.application.system_database.database;

import es.caib.invai.api.service.model.AppDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppDatabaseRepository {

    AppDatabase create(AppDatabase appDatabase);

    AppDatabase update(AppDatabase appDatabase, Long id);

    void delete(AppDatabase appDatabase);

    AppDatabase findById(Long id);

    Page<AppDatabase> findAll(Long informationSystemDbId, AppDatabaseCriteria criteria, Pageable pageable);

    boolean existsByInformationSystemDbIdAndDatabaseId(Long informationSystemDbId, Long databaseId);

    boolean existsByUniqueCombinationExcludingId(Long informationSystemDbId, Long databaseId, Long id);
}
