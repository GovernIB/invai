package es.caib.invai.back.rest.soffid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port interface abstracting outbound calls to the Soffid SCIM 2.0 API, used to list/search CAIB
 * personnel and Soffid roles, feeding the Person "getAll" style listing, the "assign a
 * responsible/authorized person" typeahead, and the security role typeahead used to populate
 * {@code SecurityRole}/{@code INV_APP_ROLE}.
 *
 * @since 1.0.4
 */
public interface SoffidClient {

    /**
     * Lists active Soffid users, optionally restricted to those whose full name contains every
     * word of {@code fullName}. A blank/{@code null} {@code fullName} performs an unfiltered
     * listing (still paginated), matching this codebase's usual {@code getAll} semantics.
     *
     * @param fullName the text to search for, matched (word by word) against the full name, or
     * {@code null}/blank to list every active user
     * @param pageable the pagination parameters; only page number and size are used, Soffid's own
     * default ordering applies
     * @return the requested page of matching Soffid users, with the total result count reported by
     * Soffid
     */
    Page<SoffidUser> search(String fullName, Pageable pageable);

    /**
     * Looks up a single active Soffid user by exact e-mail address. Used to resolve a role-transfer
     * target that has no local {@code Person} row yet.
     *
     * @param email the exact e-mail address to look up
     * @return the matching active Soffid user, or {@code null} if none is found
     */
    SoffidUser findByEmail(String email);

    /**
     * Lists Soffid roles that can be assigned to an application (feeding {@code INV_APP_ROLE}),
     * always restricted to this project's namespace (roles whose {@code name} starts with
     * {@code "INV_"} - the shared Soffid instance also hosts roles for many unrelated
     * applications), optionally further restricted to those whose {@code name} contains every word
     * of {@code name}. A blank/{@code null} {@code name} lists every {@code INV_} role (still
     * paginated), matching this codebase's usual {@code getAll} semantics.
     *
     * @param name the text to search for, matched (word by word) against the role's {@code name},
     * or {@code null}/blank to list every {@code INV_} role
     * @param pageable the pagination parameters; only page number and size are used
     * @return the requested page of matching Soffid roles, with the total result count reported by
     * Soffid
     */
    Page<SoffidRole> searchRoles(String name, Pageable pageable);
}
