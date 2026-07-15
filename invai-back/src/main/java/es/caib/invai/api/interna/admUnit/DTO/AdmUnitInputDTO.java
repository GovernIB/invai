package es.caib.invai.api.interna.admUnit.DTO;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) representing the incoming payload required to create or update
 * an Administrative Unit (AdmUnit).
 * <p>
 * Contains validation constraints with localization keys that trigger detailed client-side
 * or api-level feedback if parameters fail length or presence requirements.
 * </p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdmUnitInputDTO {

    /**
     * Unique identification corporate alphanumeric code for the administrative unit.
     * Must not be blank and have a maximum length constraint of 7 characters.
     */
    @NotBlank(message = "{validation.admunit.code.required}")
    @Size(max = 7, message = "{validation.admunit.code.size}")
    private String code;

    /**
     * Official localized descriptor name of the administrative unit (typically in Catalan).
     * Must not be blank and have a maximum length constraint of 100 characters.
     */
    @NotBlank(message = "{validation.admunit.name.required}")
    @Size(max = 100, message = "{validation.admunit.name.size}")
    private String name;

    /**
     * Spanish localized descriptor translation name of the administrative unit.
     * Must not be blank and have a maximum length constraint of 100 characters.
     */
    @NotBlank(message = "{validation.admunit.nameEs.required}")
    @Size(max = 100, message = "{validation.admunit.nameEs.size}")
    private String nameEs;
}