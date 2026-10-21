package es.caib.invai.back.service.model.catalog.mimeType;

import lombok.Getter;
import lombok.Setter;

/**
 * Domain model representing one entry of the lookup of the file formats a document can be
 * uploaded in, one per file extension.
 *
 * @since 1.0.6
 */
@Getter
@Setter
public class MimeType {

    /** Unique identifier of the file format entry. */
    private Long id;

    /** File extension in lower case and without the dot (e.g. {@code "pdf"}). */
    private String extension;

    /** Media type matching the extension (e.g. {@code "application/pdf"}). */
    private String mimeType;

    /** Display label of the format, shown as the document type (e.g. {@code "PDF"}). */
    private String name;
}
