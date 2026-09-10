package es.caib.invai.back.service.model.application.security.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.core.Application;

/**
 * Domain aggregate model representing the "Seguretat" tab anchor owned
 * by a corporate {@link Application}.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSecurity {
    /** Unique identification pointer of this security anchor. */
    private Long id;
    /** Corporate application this security anchor belongs to. */
    private Application application;
    /** Free-text observation notes attached to this security anchor. */
    private String observation;
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
