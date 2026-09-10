package es.caib.invai.back.interna.maintenance.security.securityRole.DTO;

import lombok.*;

/**
 * Data Transfer Object (DTO) used to represent the outgoing payload of a Security Role.
 * <p>
 * This object filters and exposes core database fields to client applications, shielding the
 * underlying JPA domain entity structures. Also reused for Soffid search results, where
 * {@code id} is left {@code null} since no local row exists yet (the "null id means not yet
 * cached locally" convention used elsewhere in this codebase, see {@code Person.searchSoffid}).
 * </p>
 *
 * @since 1.0.4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecurityRoleOutputDTO {

    /** The persistent database primary key identity identifier, or {@code null} if not yet cached locally. */
    private Long id;

    /** Soffid's own numeric role identifier. */
    private Long roleId;

    /** The security role descriptive label, as copied from Soffid. */
    private String name;

    /** The Soffid system (environment) this role is associated with, as copied from Soffid. */
    private String system;

    /** The human-readable role description, as copied from Soffid. */
    private String description;
}
