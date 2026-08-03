package es.caib.invai.api.persistence.repository.application.development.technology;

import es.caib.invai.api.service.model.AppTechnology;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppTechnologyRepository {

    AppTechnology create(AppTechnology appTechnology);

    AppTechnology update(AppTechnology appTechnology, Long id);

    void delete(AppTechnology appTechnology);

    AppTechnology findById(Long id);

    Page<AppTechnology> findAll(Long appDevelopmentId, AppTechnologyCriteria criteria, Pageable pageable);

    boolean existsByLayerId(Long layerId);

    boolean existsByTechnologyId(Long technologyId);
}
