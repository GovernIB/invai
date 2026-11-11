package es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the outbound response payload with the documentation
 * permissions of a role of the INVAI application.
 *
 * @since 1.0.7
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRolePermissionOutputDTO {

    /** Identifier of the role. */
    private Long roleId;

    /** Name of the role. */
    private String name;

    /** Description of the role. */
    private String description;

    /** Whether the role can read the documents of the category. */
    private boolean canRead;

    /** Whether the role can upload documents to the category. */
    private boolean canUpload;

    /** Whether the role can edit the documents of the category. */
    private boolean canEdit;

    /** Whether the role can delete the documents of the category. */
    private boolean canDelete;
}
