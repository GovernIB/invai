package es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the outbound response payload with what the logged user can
 * do with the documents of one category of the documentation module, adding up the permissions of
 * all their roles.
 *
 * @since 1.0.7
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentUserPermissionOutputDTO {

    /** Identifier of the category the permissions refer to. */
    private Long categoryId;

    /** Whether the user can read the documents of the category. */
    private boolean canRead;

    /** Whether the user can upload documents to the category. */
    private boolean canUpload;

    /** Whether the user can edit the documents of the category. */
    private boolean canEdit;

    /** Whether the user can delete the documents of the category. */
    private boolean canDelete;
}
