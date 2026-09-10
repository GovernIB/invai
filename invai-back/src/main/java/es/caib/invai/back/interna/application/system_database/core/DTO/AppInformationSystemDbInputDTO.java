package es.caib.invai.back.interna.application.system_database.core.DTO;

import es.caib.invai.back.utils.Constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * Inbound validation data transport contract containing properties required for
 * instantiation and mutation of application information system database groupings.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppInformationSystemDbInputDTO {

    /** Foreign key unique identification pointer referencing the parent corporate application. */
    @NotNull(message = "{" + Constants.VALIDATION_APP_INFORMATION_SYSTEM_DB_APPLICATION_ID + "}")
    private Long applicationId;

    /** Free-text narrative notes describing the information system grouping context. */
    private String observation;
}
