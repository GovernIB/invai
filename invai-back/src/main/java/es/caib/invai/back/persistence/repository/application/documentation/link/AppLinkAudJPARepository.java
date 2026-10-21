package es.caib.invai.back.persistence.repository.application.documentation.link;

import es.caib.invai.back.persistence.model.application.documentation.link.AppLinkAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository for {@link AppLinkAudEntity} audit snapshots.
 *
 * @since 1.0.6
 */
@Repository
public interface AppLinkAudJPARepository extends JpaRepository<AppLinkAudEntity, Long> {
}
