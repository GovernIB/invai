package es.caib.invai.back.service.model.application.responsibleAuthorized.type;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Domain aggregate model representing a single authorization type attached to an
 * {@code AppAuthorized} anchor. Audited and soft-deleted since 1.0.5.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAuthorizedTypeLink {
    /** Unique identifier. */
    private Long id;
    /** Identifier of the authorized person anchor this type is attached to. */
    private Long appAuthorizedId;
    /** Identifier of the attached authorization type catalog value. */
    private Long authorizationTypeId;
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
