package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationMetadataAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository for the {@link AppDocumentationMetadataAudEntity} audit
 * snapshots.
 *
 * @since 1.0.6
 */
@Repository
public interface AppDocumentationMetadataAudJPARepository extends JpaRepository<AppDocumentationMetadataAudEntity, Long> {
}
