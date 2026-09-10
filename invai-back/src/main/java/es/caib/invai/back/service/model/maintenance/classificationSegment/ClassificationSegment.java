package es.caib.invai.back.service.model.maintenance.classificationSegment;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring classification segment entries assignable within the Manteniments / Seguretat catalog.
 *
 * @since 1.0.4
 */
@Getter
@Setter
public class ClassificationSegment {

    /** Unique database classification segment entry index identifier key. */
    private Long id;

    /** Classification segment descriptive label string (e.g. "Segment I"). */
    private String name;

    /** Secondary descriptive classification title label string matching Spanish locales. */
    private String nameEs;

    /** Timestamp when the classification segment entry was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the classification segment entry. */
    private String createdBy;
    /** Timestamp when the classification segment entry was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the classification segment entry. */
    private String updatedBy;
    /** Timestamp when the classification segment entry was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the classification segment entry. */
    private String deletedBy;
}
