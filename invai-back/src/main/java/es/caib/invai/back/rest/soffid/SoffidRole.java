package es.caib.invai.back.rest.soffid;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Raw wire DTO mapping a single Soffid SCIM 2.0 {@code Role} resource. Only the fields needed to
 * feed the security role search typeahead are modelled; every other SCIM attribute
 * ({@code informationSystemName}, {@code ownerRoles}, {@code meta}, etc.) is intentionally ignored.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
public class SoffidRole {

    /** Soffid's own numeric identifier for the role. */
    private Long id;

    /** Unique role identifier/code, as defined in Soffid (e.g. {@code "AD role"}). */
    private String name;

    /** Human-readable description of the role. */
    private String description;

    /** The Soffid system (environment) this role is associated with. */
    private String system;
}
