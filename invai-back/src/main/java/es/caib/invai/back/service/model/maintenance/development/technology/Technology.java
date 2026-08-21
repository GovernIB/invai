package es.caib.invai.back.service.model.maintenance.development.technology;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import es.caib.invai.back.service.model.maintenance.development.layer.Layer;

/**
 * Domain entity model configuring catalog technologies organized under an architecture layer.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class Technology {

    /** Unique identification pointer of the technology. */
    private Long id;

    /** Name of the technology. */
    private String name;

    /** Architecture layer this technology belongs to. */
    private Layer layer;

    /** Timestamp at which the technology was created. */
    private LocalDateTime createdAt;
    /** Username that created the technology. */
    private String createdBy;
    /** Timestamp at which the technology was last updated. */
    private LocalDateTime updatedAt;
    /** Username that last updated the technology. */
    private String updatedBy;
    /** Timestamp at which the technology was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** Username that logically deleted the technology. */
    private String deletedBy;
}
