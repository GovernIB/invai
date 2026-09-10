package es.caib.invai.back.interna.application.security.role.DTO;

import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.interna.maintenance.security.securityRole.DTO.SecurityRoleOutputDTO;
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
}
