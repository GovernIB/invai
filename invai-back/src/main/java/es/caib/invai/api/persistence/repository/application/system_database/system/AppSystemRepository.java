package es.caib.invai.api.persistence.repository.application.system_database.system;

import es.caib.invai.api.service.model.AppSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppSystemRepository {

    AppSystem create(AppSystem appSystem);

    AppSystem update(AppSystem appSystem, Long id);

    void delete(AppSystem appSystem);

    AppSystem findById(Long id);

    Page<AppSystem> findAll(Long informationSystemDbId, AppSystemCriteria criteria, Pageable pageable);

    boolean existsByInformationSystemDbAndSystem(Long informationSystemDbId, Long systemId);

    boolean existsByInformationSystemDbAndSystemAndIdNot(Long informationSystemDbId, Long systemId, Long id);
}
