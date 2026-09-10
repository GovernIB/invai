package es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO;

import es.caib.invai.back.utils.Constants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) defining the inbound API request payload structure
 * for registering or modifying PersonalDataProcessing resource data entries.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonalDataProcessingInputDTO {

    /** Personal data processing entry descriptive label string (e.g. "Tractament de nomines"). */
    @NotBlank(message = "{" + Constants.VALIDATION_PERSONALDATAPROCESSING_NAME_REQUIRED + "}")
    @Size(max = 150, message = "{" + Constants.VALIDATION_PERSONALDATAPROCESSING_NAME_SIZE + "}")
    private String name;

    /**
     * Secondary translated description text variant explicitly matching Spanish locale records.
     * Constraint demands content presence capped at a maximum sequence of 100 characters.
     */
    @NotBlank(message = "{" + Constants.VALIDATION_PERSONALDATAPROCESSING_NAME_ES_REQUIRED + "}")
    @Size(max = 100, message = "{" + Constants.VALIDATION_PERSONALDATAPROCESSING_NAME_ES_SIZE + "}")
    private String nameEs;
}
