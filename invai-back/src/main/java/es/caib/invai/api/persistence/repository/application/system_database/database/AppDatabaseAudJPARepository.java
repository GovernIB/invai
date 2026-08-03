package es.caib.invai.api.persistence.repository.application.system_database.database;

import es.caib.invai.api.persistence.model.AppDatabaseAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppDatabaseAudJPARepository extends JpaRepository<AppDatabaseAudEntity, Long> {
}
