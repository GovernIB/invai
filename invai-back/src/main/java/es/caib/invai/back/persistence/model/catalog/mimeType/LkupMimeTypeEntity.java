package es.caib.invai.back.persistence.model.catalog.mimeType;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * JPA persistent entity representing one entry of the lookup of the file formats a document can
 * be uploaded in, one per file extension.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@Entity
@Table(name = "INV_LKUP_MIME_TYPE")
public class LkupMimeTypeEntity {

    /** Unique identifier of the file format entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_LKUP_MIME_TYPE_SEQ")
    @SequenceGenerator(name = "INV_LKUP_MIME_TYPE_SEQ", sequenceName = "INV_LKUP_MIME_TYPE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** File extension in lower case and without the dot (e.g. {@code "pdf"}). */
    @Column(name = "EXTENSION", nullable = false)
    private String extension;

    /** Media type matching the extension (e.g. {@code "application/pdf"}). */
    @Column(name = "MIME_TYPE", nullable = false)
    private String mimeType;

    /** Display label of the format, shown as the document type (e.g. {@code "PDF"}). */
    @Column(name = "NAME", nullable = false)
    private String name;
}
