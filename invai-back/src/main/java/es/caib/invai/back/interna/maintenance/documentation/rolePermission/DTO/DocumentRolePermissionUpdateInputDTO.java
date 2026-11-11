package es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO;

import es.caib.invai.back.utils.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object (DTO) defining the inbound payload that sets the documentation permissions
 * of several roles at once in a category.
 *
 * @since 1.0.7
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRolePermissionUpdateInputDTO {

    /** Permissions to set, one entry per role; the roles that are not listed are left as they are. */
    @NotEmpty(message = "{" + Constants.VALIDATION_DOCROLEPERM_ITEMS + "}")
    @Valid
    private List<DocumentRolePermissionInputDTO> permissions;
}
