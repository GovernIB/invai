package es.caib.invai.back.interna.application.security.risk.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * Inbound validation data transport contract containing properties required for
 * instantiation and mutation of application security risks.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppSecurityRiskInputDTO {

    /** Foreign key unique identification pointer referencing the parent security anchor. */
    @NotNull(message = "{validation.appsecurityrisk.appSecurityId}")
    private Long appSecurityId;

    /** Foreign key unique identification pointer referencing the security level lookup. */
    private Long levelId;

    /** Free-text description of this security risk. */
    private String description;

    /** Foreign key unique identification pointer referencing the functional business field ("Àmbit"). */
    private Long fieldId;
}
