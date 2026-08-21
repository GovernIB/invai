package es.caib.invai.back.service.model.maintenance.general.systemType;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain business model tracking standard system architectural types, engine patterns,
 * or technology runtime tiers.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class SystemType {

    /** Unique persistent sequence structural index row key mapping technology configurations. */
    private Long id;

    /** System category architecture definition designator label string (typically Catalan). */
    private String name;

    /** System category architecture alternate variant definition designator layout string matching Spanish. */
    private String nameEs;

    /** Timestamp at which the system type record was created. */
    private LocalDateTime createdAt;
    /** Username of the operator who created the system type record. */
    private String createdBy;
    /** Timestamp at which the system type record was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the operator who last updated the system type record. */
    private String updatedBy;
    /** Timestamp marking when the system type was logically soft-deleted, or {@code null} if active. */
    private LocalDateTime deletedAt;
    /** Username of the operator who logically soft-deleted the system type record. */
    private String deletedBy;
}