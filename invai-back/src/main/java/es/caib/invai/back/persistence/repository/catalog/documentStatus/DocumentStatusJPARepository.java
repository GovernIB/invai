package es.caib.invai.back.persistence.repository.catalog.documentStatus;

import es.caib.invai.back.persistence.model.catalog.documentStatus.LkupDocumentStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository for the document status lookup entries.
 *
 * @since 1.0.6
 */
@Repository
public interface DocumentStatusJPARepository extends JpaRepository<LkupDocumentStatusEntity, Long> {
}
