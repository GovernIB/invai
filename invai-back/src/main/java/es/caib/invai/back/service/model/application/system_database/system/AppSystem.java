package es.caib.invai.back.service.model.application.system_database.system;

import lombok.*;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.maintenance.systems.system.System;

/**
 * Domain model representing the association between an {@link AppInformationSystemDb}
 * and a {@link System} it relies on.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSystem {
    /** Unique identification pointer of this application-system association. */
    private Long id;
    /** Information system grouping this association belongs to. */
    private AppInformationSystemDb informationSystemDb;
    /** System catalog entry associated with the information system grouping. */
    private System system;
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