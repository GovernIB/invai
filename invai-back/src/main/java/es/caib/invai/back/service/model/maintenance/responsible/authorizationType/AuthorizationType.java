package es.caib.invai.back.service.model.maintenance.responsible.authorizationType;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring authorization types assignable within the Manteniments / Responsables catalog.
 *
 * @since 1.0.3
 */
@Getter
@Setter
public class AuthorizationType {

    /** Unique database authorization type index identifier key. */
    private Long id;

    /** Authorization type descriptive label string (e.g. "Firmar peticiones"). */
    private String name;

    /** Secondary descriptive classification title label string matching Spanish locales. */
    private String nameEs;

    /** Timestamp when the authorization type was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the authorization type. */
    private String createdBy;
    /** Timestamp when the authorization type was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the authorization type. */
    private String updatedBy;
    /** Timestamp when the authorization type was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the authorization type. */
    private String deletedBy;
}
