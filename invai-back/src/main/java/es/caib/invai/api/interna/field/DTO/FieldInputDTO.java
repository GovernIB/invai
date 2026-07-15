package es.caib.invai.api.interna.field.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) capturing incoming parameter fields payload parameters
 * required to build or manipulate an operational business Field sector boundary entry.
 * <p>
 * Enforces declarative structural validations linked to corporate validation error bundle mapping parameters.
 * </p>
 *
 * @since 1.0.0
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
    @NotBlank(message = "{validation.field.name.required}")
    @Size(max = 50, message = "{validation.field.name.size}")
    private String name;

    /**
     * Alternate operational field area text description variable matching Spanish localized parameters.
     * Must be structurally filled out with text capped at a total threshold length of 100 characters.
     */
    @NotBlank(message = "{validation.field.nameEs.required}")
    @Size(max = 100, message = "{validation.field.nameEs.size}")
    private String nameEs;
}