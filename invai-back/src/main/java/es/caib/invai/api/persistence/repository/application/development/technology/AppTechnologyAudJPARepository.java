package es.caib.invai.api.persistence.repository.application.development.technology;

import es.caib.invai.api.persistence.model.AppTechnologyAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppTechnologyAudJPARepository extends JpaRepository<AppTechnologyAudEntity, Long> {
}
