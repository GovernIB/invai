package es.caib.invai.back.interna.application.security.measure.DTO;

import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.interna.maintenance.security.securityMeasureType.DTO.SecurityMeasureTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Outbound transport payload detailing technical context mappings of the target
 * application security measure entity definitions.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSecurityMeasureOutputDTO {

    /** Unique identification pointer of this security measure. */
    private Long id;
    /** Security anchor this measure belongs to. */
    private AppSecurityOutputDTO appSecurity;
    /** Security measure type catalog entry classifying this measure. */
    private SecurityMeasureTypeOutputDTO type;
    /** ENS requirement catalog entry this measure is associated with. */
    private EnsRequirementOutputDTO ensRequirement;
    /** Free-text description of the applied security measure. */
    private String description;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
