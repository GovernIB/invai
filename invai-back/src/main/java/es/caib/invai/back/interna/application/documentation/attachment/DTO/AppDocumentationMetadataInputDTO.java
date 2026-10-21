package es.caib.invai.back.interna.application.documentation.attachment.DTO;

import es.caib.invai.back.utils.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling one key-value metadata sent along with a document or with an
 * attachment. It always travels inside the payload of one of them, never on its own.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationMetadataInputDTO {

    /** Key of the metadata (e.g. {@code eni.claseDocumento}), unique among the metadata sent together. */
    @NotBlank(message = "{" + Constants.VALIDATION_DOCUMENTATION_METADATA_KEY_REQUIRED + "}")
    @Size(max = 80, message = "{" + Constants.VALIDATION_DOCUMENTATION_METADATA_KEY_SIZE + "}")
    private String key;

    /** Value of the metadata, free text. */
    @NotBlank(message = "{" + Constants.VALIDATION_DOCUMENTATION_METADATA_VALUE_REQUIRED + "}")
    @Size(max = 1024, message = "{" + Constants.VALIDATION_DOCUMENTATION_METADATA_VALUE_SIZE + "}")
    private String value;
}
