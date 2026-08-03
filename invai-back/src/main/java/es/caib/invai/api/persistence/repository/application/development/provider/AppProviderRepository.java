package es.caib.invai.api.persistence.repository.application.development.provider;

import es.caib.invai.api.service.model.AppProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppProviderRepository {

    AppProvider create(AppProvider appProvider);

    AppProvider update(AppProvider appProvider, Long id);

    void delete(AppProvider appProvider);

    AppProvider findById(Long id);

    Page<AppProvider> findAll(Long appDevelopmentId, AppProviderCriteria criteria, Pageable pageable);

    boolean existsByRoleId(Long roleId);
}
