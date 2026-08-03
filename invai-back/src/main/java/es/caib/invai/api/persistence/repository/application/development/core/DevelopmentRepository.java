package es.caib.invai.api.persistence.repository.application.development.core;

import es.caib.invai.api.service.model.AppDevelopment;

public interface DevelopmentRepository {

    AppDevelopment create(AppDevelopment appDevelopment);

    AppDevelopment update(AppDevelopment appDevelopment, Long id);

    void delete(AppDevelopment appDevelopment);

    AppDevelopment findById(Long id);

    AppDevelopment findByApplicationId(Long applicationId);
}
