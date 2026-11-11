package es.caib.invai.back.persistence.model.application.documentation.email;

import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionEntity;
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
 * JPA persistent entity representing an attachment of an e-mail of the history. The text
 * fields keep what was sent even if the document changes later.
 *
 * @since 1.0.7
 */
@Entity
@Table(name = "INV_APP_DOC_EMAIL_ITEM")
@Getter
@Setter
public class AppEmailItemEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated database identifier of the item. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DOC_EMAIL_ITEM_SEQ")
    @SequenceGenerator(name = "INV_APP_DOC_EMAIL_ITEM_SEQ", sequenceName = "INV_APP_DOC_EMAIL_ITEM_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Sent e-mail the item belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_DOC_EMAIL_ID", nullable = false)
    private AppEmailEntity appEmail;

    /** Version of the document that was attached. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_DOC_ATTACHMENT_VERSION_ID", nullable = false)
    private AppDocumentationAttachmentVersionEntity version;

    /** Title of the document when it was sent. */
    @Column(name = "TITLE", nullable = false, length = 250)
    private String title;

    /** Version label of the attached file when it was sent. */
    @Column(name = "VERSION_LABEL", nullable = false, length = 50)
    private String versionLabel;

    /** Name of the attached file when it was sent. */
    @Column(name = "FILE_NAME", nullable = false, length = 255)
    private String fileName;
}
