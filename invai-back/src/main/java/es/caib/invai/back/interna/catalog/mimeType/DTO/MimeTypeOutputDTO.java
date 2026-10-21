package es.caib.invai.back.interna.catalog.mimeType.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a file format
 * lookup entry.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MimeTypeOutputDTO {

    /** Unique identifier of the file format entry. */
    private Long id;

    /** File extension in lower case and without the dot (e.g. {@code "pdf"}). */
    private String extension;

    /** Media type matching the extension (e.g. {@code "application/pdf"}). */
    private String mimeType;

    /** Display label of the format, shown as the document type (e.g. {@code "PDF"}). */
    private String name;
}
