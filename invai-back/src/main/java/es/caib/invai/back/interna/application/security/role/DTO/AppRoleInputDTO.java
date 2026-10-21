package es.caib.invai.back.interna.application.security.role.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * Inbound validation data transport contract containing properties required for
 * instantiation and mutation of application role assignments.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppRoleInputDTO {

    /** Foreign key unique identification pointer referencing the parent security anchor. */
    @NotNull(message = "{validation.approle.appSecurityId}")
    private Long appSecurityId;

    /**
     * Soffid's own numeric identifier of the role to assign (the {@code roleId} Soffid's role search
     * returns), not the local catalog's identifier. The role's data is asked of Soffid and kept in the
     * local security role catalog.
     */
    @NotNull(message = "{validation.approle.securityRoleId}")
    private Long securityRoleId;
}
