package es.caib.invai.back.persistence.repository.catalog.appSection;

import es.caib.invai.back.persistence.model.catalog.appSection.LkupAppSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository for the application section lookup entries.
 *
 * @since 1.0.6
 */
@Repository
public interface AppSectionJPARepository extends JpaRepository<LkupAppSectionEntity, Long> {
}
