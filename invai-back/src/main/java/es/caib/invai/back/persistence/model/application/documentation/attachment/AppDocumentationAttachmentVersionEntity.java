package es.caib.invai.back.persistence.model.application.documentation.attachment;

import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.catalog.mimeType.LkupMimeTypeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.sql.Blob;

/**
 * JPA persistent entity representing each file uploaded for a document, content included. The
 * {@link #content} is mapped as a {@link Blob} locator, so loading a version doesn't read the
 * bytes until they are asked for; listings must still avoid selecting it.
 *
 * @since 1.0.6
 */
@Entity
@Table(name = "INV_APP_DOC_ATTACHMENT_VERSION")
@Getter
@Setter
public class AppDocumentationAttachmentVersionEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated database identifier of the version. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DOC_ATT_VER_SEQ")
    @SequenceGenerator(name = "INV_APP_DOC_ATT_VER_SEQ", sequenceName = "INV_APP_DOC_ATT_VER_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Document this file is a version of. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_DOC_ATTACHMENT_ID", nullable = false)
    private AppDocumentationAttachmentEntity appDocumentationAttachment;

    /** Version typed in by hand by whoever uploaded the file (e.g. {@code "2.1"}). */
    @Column(name = "VERSION_LABEL", nullable = false, length = 50)
    private String versionLabel;

    /** Original name of the uploaded file. */
    @Column(name = "FILE_NAME", nullable = false, length = 255)
    private String fileName;

    /** File format of the uploaded file. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MIME_TYPE_ID", nullable = false)
    private LkupMimeTypeEntity mimeType;

    /** Size of the uploaded file in bytes. */
    @Column(name = "FILE_SIZE", nullable = false)
    private Long fileSize;

    /** Hexadecimal SHA-256 of the uploaded content. */
    @Column(name = "CHECKSUM_SHA256", nullable = false, length = 64)
    private String checksumSha256;

    /** Whether this is the current version of the document. */
    @Column(name = "IS_CURRENT", nullable = false)
    private boolean current;

    /** Optional note describing what changed in this version. */
    @Column(name = "CHANGE_NOTE", length = 500)
    private String changeNote;

    /** Bytes of the uploaded file; immutable once stored. */
    @Lob
    @Column(name = "CONTENT", nullable = false, updatable = false)
    private Blob content;
}
