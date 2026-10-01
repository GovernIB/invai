package es.caib.invai.back.persistence.repository.application.data;

import es.caib.invai.back.persistence.model.application.data.AppDataAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository providing persistence for {@link AppDataAudEntity} audit rows.
 *
 * @since 1.0.5
 */
public interface AppDataAudJPARepository extends JpaRepository<AppDataAudEntity, Long> {
}
