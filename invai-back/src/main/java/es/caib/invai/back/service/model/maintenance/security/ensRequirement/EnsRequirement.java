package es.caib.invai.back.service.model.maintenance.security.ensRequirement;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring ENS (Esquema Nacional de Seguridad) requirement entries
 * assignable within the Manteniments / Seguretat catalog.
 *
 * @since 1.0.4
 */
@Getter
@Setter
public class EnsRequirement {

    /** Unique database ENS requirement entry index identifier key. */
    private Long id;

    /** ENS requirement descriptive label string. */
    private String name;

    /** Secondary descriptive classification title label string matching Spanish locales. */
    private String nameEs;

    /** Timestamp when the ENS requirement entry was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the ENS requirement entry. */
    private String createdBy;
    /** Timestamp when the ENS requirement entry was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the ENS requirement entry. */
    private String updatedBy;
    /** Timestamp when the ENS requirement entry was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the ENS requirement entry. */
    private String deletedBy;
}
