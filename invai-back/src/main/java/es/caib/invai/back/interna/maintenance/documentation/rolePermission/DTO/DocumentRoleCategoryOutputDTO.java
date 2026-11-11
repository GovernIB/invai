package es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO;

import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the outbound response payload with a category of the
 * documentation module, in full, and what a role can do with its documents.
 *
 * @since 1.0.7
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRoleCategoryOutputDTO {

    /** The category, with its subcategories. */
    private DocumentCategoryOutputDTO category;

    /** Whether the role can read the documents of the category. */
    private boolean canRead;

    /** Whether the role can upload documents to the category. */
    private boolean canUpload;

    /** Whether the role can edit the documents of the category. */
    private boolean canEdit;

    /** Whether the role can delete the documents of the category. */
    private boolean canDelete;
}
