package es.caib.invai.back.interna.maintenance.general.systemType.DTO;

import es.caib.invai.back.utils.Constants;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) capturing input arguments properties required to instantiate
 * or modify an infrastructure architecture classification System Type catalog entry.
 * <p>
 * Embeds data integrity validation annotations mapped over externalized internationalization system strings.
 * </p>
 *
 * @since 1.0.1
 */
@Getter
@Setter
public class SystemTypeInputDTO {

    /**
     * System class architecture type designator structural description label string (typically in Catalan).
     * Field content values are required and are capped below an execution layout ceiling of 100 characters.
     */
    @NotBlank(message = "{" + Constants.VALIDATION_SYSTEMTYPE_NAME_REQUIRED + "}")
    @Size(max = 100, message = "{" + Constants.VALIDATION_SYSTEMTYPE_NAME_SIZE + "}")
    private String name;

    /**
     * Alternate architectural technology category pattern mapping title description for Spanish localized outputs.
     * Field content values are required and are capped below an execution layout ceiling of 100 characters.
     */
    @NotBlank(message = "{" + Constants.VALIDATION_SYSTEMTYPE_NAME_ES_REQUIRED + "}")
    @Size(max = 100, message = "{" + Constants.VALIDATION_SYSTEMTYPE_NAME_ES_SIZE + "}")
    private String nameEs;
}