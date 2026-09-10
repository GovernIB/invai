package es.caib.invai.back.service.model.maintenance.security.webContext;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring web contexts assignable within the Manteniments / Seguretat catalog.
 *
 * @since 1.0.4
 */
@Getter
@Setter
public class WebContext {

    /** Unique database web context index identifier key. */
    private Long id;

    /** Web context descriptive label string (e.g. "Firmar peticiones"). */
    private String name;

    /** Secondary descriptive classification title label string matching Spanish locales. */
    private String nameEs;

    /** Timestamp when the web context was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the web context. */
    private String createdBy;
    /** Timestamp when the web context was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the web context. */
    private String updatedBy;
    /** Timestamp when the web context was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the web context. */
    private String deletedBy;
}
