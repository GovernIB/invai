package es.caib.invai.api.interna.rest.soffid;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Raw wire DTO mapping a single Soffid SCIM 2.0 {@code RoleAccount} resource - a role currently
 * granted to an account. Only the fields needed to rebuild a {@link SoffidRole}-shaped result are
 * modelled; every other SCIM attribute ({@code accountName}, {@code userFullName}, {@code meta},
 * etc.) is intentionally ignored.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
public class SoffidUserRole {

    /** Soffid's own numeric identifier for the role currently granted to the account. */
    private Long roleId;

    /** Name of the role currently granted to the account. */
    private String roleName;

    /** The Soffid system (environment) this role is associated with. */
    private String system;

    /** Human-readable description of the role. */
    private String roleDescription;
}
