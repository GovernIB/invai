package es.caib.invai.back.rest.soffid;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Raw wire DTO mapping a single Soffid SCIM 2.0 {@code User} resource. Only the fields needed to
 * feed the person search typeahead are modelled; every other SCIM attribute (mailServer,
 * homeServer, meta, links, etc.) is intentionally ignored.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
public class SoffidUser {

    /** First name of the Soffid user. */
    private String firstName;

    /** Last name of the Soffid user. */
    private String lastName;

    /** Contact e-mail address of the Soffid user. */
    private String emailAddress;

    /** Whether the Soffid user account is currently active. */
    private boolean active;
}
