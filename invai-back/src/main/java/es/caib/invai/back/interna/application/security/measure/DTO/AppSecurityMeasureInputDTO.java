package es.caib.invai.back.interna.application.security.measure.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * Inbound validation data transport contract containing properties required for
 * instantiation and mutation of application security measures.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppSecurityMeasureInputDTO {

    /** Foreign key unique identification pointer referencing the parent security anchor. */
    @NotNull(message = "{validation.appsecuritymeasure.appSecurityId}")
    private Long appSecurityId;

    /** Foreign key unique identification pointer referencing the security measure type. */
    private Long typeId;

    /** Foreign key unique identification pointer referencing the ENS requirement. */
    private Long ensRequirementId;

    /** Free-text description of the applied security measure. */
    private String description;
}
