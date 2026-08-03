package es.caib.invai.api.persistence.repository.application.system_database.core;

import es.caib.invai.api.persistence.model.AppInformationSystemDbAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppInformationSystemDbAudJPARepository extends JpaRepository<AppInformationSystemDbAudEntity, Long> {
}
