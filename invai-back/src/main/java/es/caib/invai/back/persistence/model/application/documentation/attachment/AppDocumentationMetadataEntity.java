package es.caib.invai.back.persistence.model.application.documentation.attachment;

import es.caib.invai.back.persistence.model.BaseEntity;
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
 * JPA persistent entity representing one extensible key-value metadata of a document, or of one
 * of its attachments.
 *
 * @since 1.0.6
 */
@Entity
@Table(name = "INV_APP_DOCUMENTATION_METADATA")
@Getter
@Setter
public class AppDocumentationMetadataEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated database identifier of the metadata. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DOC_META_SEQ")
    @SequenceGenerator(name = "INV_APP_DOC_META_SEQ", sequenceName = "INV_APP_DOC_META_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Attachment the metadata belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_DOC_ATTACHMENT_VERSION_ID", nullable = false)
    private AppDocumentationAttachmentVersionEntity appDocumentationAttachmentVersion;

    /** Key of the metadata, unique among the active metadata of the same attachment. */
    @Column(name = "META_KEY", nullable = false, length = 80)
    private String metaKey;

    /** Value of the metadata, free text. */
    @Column(name = "META_VALUE", nullable = false, length = 1024)
    private String metaValue;
}
