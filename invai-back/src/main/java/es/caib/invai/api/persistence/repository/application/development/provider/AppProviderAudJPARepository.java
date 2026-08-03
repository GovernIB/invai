package es.caib.invai.api.persistence.repository.application.development.provider;

import es.caib.invai.api.persistence.model.AppProviderAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppProviderAudJPARepository extends JpaRepository<AppProviderAudEntity, Long> {
}
