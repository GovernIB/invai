package es.caib.invai.api.persistence.repository.application.development.core;

import es.caib.invai.api.persistence.model.DevelopmentAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevelopmentAudJPARepository extends JpaRepository<DevelopmentAudEntity, Long> {
}
