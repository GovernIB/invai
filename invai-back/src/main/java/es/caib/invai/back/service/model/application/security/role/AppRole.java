package es.caib.invai.back.service.model.application.security.role;

import lombok.*;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatusEnum;
import es.caib.invai.back.service.model.maintenance.security.securityRole.SecurityRole;

/**
 * Domain model representing a security role assigned to an {@link AppSecurity}
 * anchor's security profile.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppRole {
    /** Unique identification pointer of this application-role association. */
    private Long id;
    /** Security anchor this role assignment belongs to. */
    private AppSecurity appSecurity;
    /** Security role catalog entry assigned to the application's security profile. */
    private SecurityRole securityRole;
    /** Timestamp at which this record was created. */
    private LocalDateTime createdAt;
    /** Identifier of the user who created this record. */
    private String createdBy;
    /** Timestamp at which this record was last updated. */
    private LocalDateTime updatedAt;
    /** Identifier of the user who last updated this record. */
    private String updatedBy;
    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** Identifier of the user who logically deleted this record, or {@code null} if still active. */
    private String deletedBy;
    /**
     * Validation state of this assignment against the application's prefix: {@code VALIDATED} if
     * the role's name starts with the prefix followed by an underscore, {@code NOT_VALIDATED} if it
     * doesn't, {@code MANUAL} if a person vouched for it anyway.
     */
    private ValidationStatusEnum validationStatus;
    /** Timestamp of the manual validation, populated only when the state is {@code MANUAL}. */
    private LocalDateTime manualValidatedAt;
    /** User who validated the assignment manually, populated only when the state is {@code MANUAL}. */
    private String manualValidatedBy;
}
