package es.caib.invai.api.persistence.repository.application.system_database.core;

import es.caib.invai.api.service.model.AppInformationSystemDb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface AppInformationSystemDbRepository {

    AppInformationSystemDb create(AppInformationSystemDb appInformationSystemDb);

    AppInformationSystemDb update(AppInformationSystemDb appInformationSystemDb, Long id);

    void delete(AppInformationSystemDb appInformationSystemDb);

    AppInformationSystemDb findById(Long id);

    AppInformationSystemDb findByApplicationId(Long applicationId);
}
