package es.caib.invai.api.interna.rest.soffid;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Raw wire DTO mapping a single Soffid SCIM 2.0 {@code User} resource. Only the fields needed to
 * feed the person search typeahead and resolve a user's DIR3CAIB code are modelled; every other
 * SCIM attribute (mailServer, homeServer, meta, links, etc.) is intentionally ignored. Secondary
 * group memberships are NOT modelled here: the real Soffid {@code User} resource has no such
 * field - they live on a separate {@code GroupUser} resource instead - so DIR3 resolution only
 * ever considers {@link #primaryGroup}.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
public class SoffidUser {

    /** Soffid's own numeric identifier for this user. */
    private Long id;

    /** Soffid username (código de usuario), e.g. "u00629". */
    private String userName;

    /** First name of the Soffid user. */
    private String firstName;

    /** Last name of the Soffid user. */
    private String lastName;

    /** Contact the e-mail address of the Soffid user. */
    private String emailAddress;

    /** Whether the Soffid user account is currently active. */
    private boolean active;

    /** Short name of the user's primary Soffid group (e.g. {@code "sgaip"}), or {@code null} if none. */
    private String primaryGroup;
}
