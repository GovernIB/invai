package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository for the {@link AppDocumentationAttachmentVersionAudEntity} audit snapshots.
 *
 * @since 1.0.6
 */
@Repository
public interface AppDocumentationAttachmentVersionAudJPARepository extends JpaRepository<AppDocumentationAttachmentVersionAudEntity, Long> {
}
