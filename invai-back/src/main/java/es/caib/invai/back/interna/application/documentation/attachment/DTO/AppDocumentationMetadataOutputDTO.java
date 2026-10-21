package es.caib.invai.back.interna.application.documentation.attachment.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling one key-value metadata of a document or of an attachment, as
 * shown inside the detail of the document.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationMetadataOutputDTO {

    /** Key of the metadata. */
    private String key;

    /** Value of the metadata. */
    private String value;
}
