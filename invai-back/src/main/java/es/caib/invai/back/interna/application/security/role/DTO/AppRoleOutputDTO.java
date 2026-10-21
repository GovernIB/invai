package es.caib.invai.back.interna.application.security.role.DTO;

import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.interna.maintenance.security.securityRole.DTO.SecurityRoleOutputDTO;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Outbound transport payload detailing technical context mappings of the target
 * application role assignment entity definitions.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppRoleOutputDTO {

    /** Unique identification pointer of this application-role association. */
    private Long id;
    /** Security anchor this role assignment belongs to. */
    private AppSecurityOutputDTO appSecurity;
    /** Security role catalog entry assigned to the application's security profile. */
    private SecurityRoleOutputDTO securityRole;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /**
     * Validation state of this assignment against the application's prefix: {@code VALIDATED} if
     * the role's name starts with the prefix followed by an underscore, {@code NOT_VALIDATED} if it
     * doesn't (the warning the "Seguretat" tab shows), {@code MANUAL} if a person vouched for it
     * anyway.
     *
     * @since 1.0.6
     */
    private ValidationStatusEnum validationStatus;
    /**
     * Timestamp of the manual validation, populated only when the state is {@code MANUAL}.
     *
     * @since 1.0.6
     */
    private LocalDateTime manualValidatedAt;
    /**
     * User who validated the assignment manually, populated only when the state is {@code MANUAL}.
     *
     * @since 1.0.6
     */
    private String manualValidatedBy;
}
