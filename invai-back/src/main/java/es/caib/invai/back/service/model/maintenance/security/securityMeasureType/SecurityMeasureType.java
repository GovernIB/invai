package es.caib.invai.back.service.model.maintenance.security.securityMeasureType;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring security measure type entries assignable within the Manteniments / Seguretat catalog.
 *
 * @since 1.0.4
 */
@Getter
@Setter
public class SecurityMeasureType {

    /** Unique database security measure type entry index identifier key. */
    private Long id;

    /** Security measure type descriptive label string (e.g. "Organitzativa"). */
    private String name;

    /** Secondary descriptive classification title label string matching Spanish locales. */
    private String nameEs;

    /** Timestamp when the security measure type entry was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the security measure type entry. */
    private String createdBy;
    /** Timestamp when the security measure type entry was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the security measure type entry. */
    private String updatedBy;
    /** Timestamp when the security measure type entry was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the security measure type entry. */
    private String deletedBy;
}
