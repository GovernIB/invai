package es.caib.invai.api.interna.systemType.DTO;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) capturing input arguments properties required to instantiate
 * or modify an infrastructure architecture classification System Type catalog entry.
 * <p>
 * Embeds data integrity validation annotations mapped over externalized internationalization system strings.
 * </p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class SystemTypeInputDTO {

    /**
     * System class architecture type designator structural description label string (typically in Catalan).
     * Field content values are required and are capped below an execution layout ceiling of 100 characters.
     */
    @NotBlank(message = "{validation.systemtype.name.required}")
    @Size(max = 100, message = "{validation.systemtype.name.size}")
    private String name;

    /**
     * Alternate architectural technology category pattern mapping title description for Spanish localized outputs.
     * Field content values are required and are capped below an execution layout ceiling of 100 characters.
     */
    @NotBlank(message = "{validation.systemtype.nameEs.required}")
    @Size(max = 100, message = "{validation.systemtype.nameEs.size}")
    private String nameEs;
}