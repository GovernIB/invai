package es.caib.invai.back.persistence.model.application.documentation.attachment;

import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.catalog.appSection.LkupAppSectionEntity;
import es.caib.invai.back.persistence.model.catalog.documentStatus.LkupDocumentStatusEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * JPA persistent entity representing a document attached to an application (the "Adjunts" list of
 * the Documentació tab). The uploaded files live in its attachments, see
 * {@link AppDocumentationAttachmentVersionEntity}.
 *
 * @since 1.0.6
 */
@Entity
@Table(name = "INV_APP_DOC_ATTACHMENT")
@Getter
@Setter
public class AppDocumentationAttachmentEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated database identifier of the document. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DOC_ATT_SEQ")
    @SequenceGenerator(name = "INV_APP_DOC_ATT_SEQ", sequenceName = "INV_APP_DOC_ATT_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Application the document belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", nullable = false)
    private ApplicationEntity application;

    /** Title of the document. */
    @Column(name = "TITLE", nullable = false, length = 250)
    private String title;

    /** Section of the application the document is filed under. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECTION_ID", nullable = false)
    private LkupAppSectionEntity section;

    /** Most specific category of the document: a subcategory if it has one, otherwise a category. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOCUMENT_CATEGORY_ID", nullable = false)
    private DocumentCategoryEntity documentCategory;

    /** Current state of the document. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STATUS_ID", nullable = false)
    private LkupDocumentStatusEntity status;
}
