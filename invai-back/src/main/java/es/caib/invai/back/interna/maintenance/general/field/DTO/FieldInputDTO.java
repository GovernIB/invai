package es.caib.invai.back.interna.maintenance.general.field.DTO;

import es.caib.invai.back.utils.Constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) capturing incoming parameter fields payload parameters
 * required to build or manipulate an operational business Field sector boundary entry.
 * <p>
 * Enforces declarative structural validations linked to corporate validation error bundle mapping parameters.
 * </p>
 *
 * @since 1.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldInputDTO {

    /**
     * Functional area sector description title naming parameter label string (typically in Catalan).
     * Must be structurally filled out with text capped at a total threshold length of 50 characters.
     */
    @NotBlank(message = "{" + Constants.VALIDATION_FIELD_NAME_REQUIRED + "}")
    @Size(max = 50, message = "{" + Constants.VALIDATION_FIELD_NAME_SIZE + "}")
    private String name;

    /**
     * Alternate operational field area text description variable matching Spanish localized parameters.
     * Must be structurally filled out with text capped at a total threshold length of 100 characters.
     */
    @NotBlank(message = "{" + Constants.VALIDATION_FIELD_NAME_ES_REQUIRED + "}")
    @Size(max = 100, message = "{" + Constants.VALIDATION_FIELD_NAME_ES_SIZE + "}")
    private String nameEs;
}