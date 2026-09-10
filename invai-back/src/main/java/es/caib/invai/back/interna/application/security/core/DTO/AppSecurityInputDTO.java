package es.caib.invai.back.interna.application.security.core.DTO;

import es.caib.invai.back.utils.Constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * Inbound validation data transport contract containing properties required for
 * instantiation and mutation of application security anchors.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppSecurityInputDTO {

    /** Foreign key unique identification pointer referencing the parent corporate application. */
    @NotNull(message = "{" + Constants.VALIDATION_APP_SECURITY_APPLICATION_ID + "}")
    private Long applicationId;

    /** Free-text narrative notes describing the security anchor context. */
    private String observation;
}
