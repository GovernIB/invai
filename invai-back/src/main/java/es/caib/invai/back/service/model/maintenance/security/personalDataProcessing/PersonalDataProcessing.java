package es.caib.invai.back.service.model.maintenance.security.personalDataProcessing;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring personal data processing entries assignable within the Manteniments / Seguretat catalog.
 *
 * @since 1.0.4
 */
@Getter
@Setter
public class PersonalDataProcessing {

    /** Unique database personal data processing entry index identifier key. */
    private Long id;

    /** Personal data processing entry descriptive label string (e.g. "Firmar peticiones"). */
    private String name;

    /** Secondary descriptive classification title label string matching Spanish locales. */
    private String nameEs;

    /** Timestamp when the personal data processing entry was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the personal data processing entry. */
    private String createdBy;
    /** Timestamp when the personal data processing entry was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the personal data processing entry. */
    private String updatedBy;
    /** Timestamp when the personal data processing entry was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the personal data processing entry. */
    private String deletedBy;
}
