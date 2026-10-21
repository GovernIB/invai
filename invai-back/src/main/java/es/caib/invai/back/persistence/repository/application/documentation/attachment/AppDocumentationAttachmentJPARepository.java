package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository for {@link AppDocumentationAttachmentEntity} records.
 *
 * @since 1.0.6
 */
@Repository
public interface AppDocumentationAttachmentJPARepository extends JpaRepository<AppDocumentationAttachmentEntity, Long>, JpaSpecificationExecutor<AppDocumentationAttachmentEntity> {
}
