package es.caib.invai.back.service.model.maintenance.security.identityProvider;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring identity providers assignable within the Manteniments / Seguretat catalog.
 *
 * @since 1.0.4
 */
@Getter
@Setter
public class IdentityProvider {

    /** Unique database identity provider index identifier key. */
    private Long id;

    /** Identity provider descriptive label string (e.g. "Cl@ve"). */
    private String name;

    /** Timestamp when the identity provider was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the identity provider. */
    private String createdBy;
    /** Timestamp when the identity provider was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the identity provider. */
    private String updatedBy;
    /** Timestamp when the identity provider was logically deleted, or {@code null} if it is still active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the identity provider. */
    private String deletedBy;
}
