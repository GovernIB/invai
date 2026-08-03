package es.caib.invai.api.persistence.repository.application.system_database.system;

import es.caib.invai.api.persistence.model.AppSystemAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSystemAudJPARepository extends JpaRepository<AppSystemAudEntity, Long> {
}
