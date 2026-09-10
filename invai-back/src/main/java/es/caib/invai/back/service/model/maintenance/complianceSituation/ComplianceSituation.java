package es.caib.invai.back.service.model.maintenance.complianceSituation;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring compliance situation entries assignable within the Manteniments / Seguretat catalog.
 *
 * @since 1.0.4
 */
@Getter
@Setter
public class ComplianceSituation {

    /** Unique database compliance situation entry index identifier key. */
    private Long id;

    /** Compliance situation descriptive label string (e.g. "Conforme"). */
    private String name;

    /** Secondary descriptive classification title label string matching Spanish locales. */
    private String nameEs;

    /** Timestamp when the compliance situation entry was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the compliance situation entry. */
    private String createdBy;
    /** Timestamp when the compliance situation entry was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the compliance situation entry. */
    private String updatedBy;
    /** Timestamp when the compliance situation entry was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the compliance situation entry. */
    private String deletedBy;
}
