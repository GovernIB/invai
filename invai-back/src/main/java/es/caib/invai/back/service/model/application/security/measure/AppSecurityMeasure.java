package es.caib.invai.back.service.model.application.security.measure;

import lombok.*;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.maintenance.security.securityMeasureType.SecurityMeasureType;
import es.caib.invai.back.service.model.maintenance.security.ensRequirement.EnsRequirement;

/**
 * Domain model representing a security measure applied to an {@link AppSecurity}
 * anchor record.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSecurityMeasure {
    /** Unique identification pointer of this security measure. */
    private Long id;
    /** Security anchor this measure belongs to. */
    private AppSecurity appSecurity;
    /** Security measure type catalog entry classifying this measure. */
    private SecurityMeasureType type;
    /** ENS requirement catalog entry this measure is associated with. */
    private EnsRequirement ensRequirement;
    /** Free-text description of the applied security measure. */
    private String description;
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
