package es.caib.invai.back.service.model.maintenance.security.securityRole;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Business domain representation of an application Security Role.
 * <p>
 * Values are locally cached and, in a future iteration, are expected to be resolved/created
 * from the external Soffid identity system rather than being manually seeded.
 * </p>
 *
 * @since 1.0.4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecurityRole {

    /** Unique persistent sequencing index record identity. */
    private Long id;

    /** Soffid's own numeric role identifier. */
    private Long roleId;

    /** Security role descriptive label, as copied from Soffid. */
    private String name;

    /** Soffid system (environment) this role is associated with, as copied from Soffid. */
    private String system;

    /** Human-readable role description, as copied from Soffid. */
    private String description;
}
