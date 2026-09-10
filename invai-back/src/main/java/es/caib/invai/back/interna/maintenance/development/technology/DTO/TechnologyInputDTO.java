package es.caib.invai.back.interna.maintenance.development.technology.DTO;

import es.caib.invai.back.utils.Constants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) defining the inbound API request payload structure
 * for registering or modifying catalog Technology resource data entries.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TechnologyInputDTO {

    /** Name of the technology. */
    @NotBlank(message = "{" + Constants.VALIDATION_TECHNOLOGYCATALOG_NAME_REQUIRED + "}")
    @Size(max = 100, message = "{" + Constants.VALIDATION_TECHNOLOGYCATALOG_NAME_SIZE + "}")
    private String name;

    /** Foreign key unique identification pointer referencing the parent architecture layer. */
    @NotNull(message = "{" + Constants.VALIDATION_TECHNOLOGYCATALOG_LAYER_ID + "}")
    private Long layerId;
}
