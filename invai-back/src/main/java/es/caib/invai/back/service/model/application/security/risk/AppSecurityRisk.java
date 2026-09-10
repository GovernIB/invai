package es.caib.invai.back.service.model.application.security.risk;

import lombok.*;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.catalog.securityLevel.SecurityLevel;
import es.caib.invai.back.service.model.maintenance.general.field.Field;

/**
 * Domain model representing a security risk identified for an {@link AppSecurity} anchor.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSecurityRisk {
    /** Unique identification pointer of this security risk. */
    private Long id;
    /** Security anchor this risk belongs to. */
    private AppSecurity appSecurity;
    /** Security level lookup classifying the severity of this risk. */
    private SecurityLevel level;
    /** Free-text description of this security risk. */
    private String description;
    /** Functional business field ("Àmbit") this risk is associated with. */
    private Field field;
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
}
