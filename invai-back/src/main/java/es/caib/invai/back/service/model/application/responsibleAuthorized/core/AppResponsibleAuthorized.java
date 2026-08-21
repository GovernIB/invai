package es.caib.invai.back.service.model.application.responsibleAuthorized.core;

import lombok.*;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.core.Application;

/**
 * Domain aggregate model anchoring the "Responsables i Autoritzats" tab for a corporate
 * {@link Application}.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppResponsibleAuthorized {
    /** Unique identifier. */
    private Long id;
    /** Corporate application this anchor belongs to. */
    private Application application;
    /** Timestamp when this record was created. */
    private LocalDateTime createdAt;
    /** User who created this record. */
    private String createdBy;
    /** Timestamp of the last update to this record. */
    private LocalDateTime updatedAt;
    /** User who last updated this record. */
    private String updatedBy;
    /** Timestamp when this record was soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** User who soft-deleted this record. */
    private String deletedBy;
}
