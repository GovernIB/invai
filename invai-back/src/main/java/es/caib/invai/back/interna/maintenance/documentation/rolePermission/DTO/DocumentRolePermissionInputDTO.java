package es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO;

import es.caib.invai.back.utils.Constants;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) defining the permissions to set for one role of the INVAI application.
 *
 * @since 1.0.7
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRolePermissionInputDTO {

    /** Identifier of the role the permissions are set for. */
    @NotNull(message = "{" + Constants.VALIDATION_DOCROLEPERM_ROLE_ID_REQUIRED + "}")
    private Long roleId;

    /** Whether the role can read the documents of the category. */
    @NotNull(message = "{" + Constants.VALIDATION_DOCROLEPERM_PERMISSION_REQUIRED + "}")
    private Boolean canRead;

    /** Whether the role can upload documents to the category. */
    @NotNull(message = "{" + Constants.VALIDATION_DOCROLEPERM_PERMISSION_REQUIRED + "}")
    private Boolean canUpload;

    /** Whether the role can edit the documents of the category. */
    @NotNull(message = "{" + Constants.VALIDATION_DOCROLEPERM_PERMISSION_REQUIRED + "}")
    private Boolean canEdit;

    /** Whether the role can delete the documents of the category. */
    @NotNull(message = "{" + Constants.VALIDATION_DOCROLEPERM_PERMISSION_REQUIRED + "}")
    private Boolean canDelete;
}
