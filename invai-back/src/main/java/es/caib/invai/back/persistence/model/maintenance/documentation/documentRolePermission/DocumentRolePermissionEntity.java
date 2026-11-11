package es.caib.invai.back.persistence.model.maintenance.documentation.documentRolePermission;

import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.persistence.model.maintenance.role.RoleEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * JPA persistent entity holding what a role of the INVAI application can do with the documents of
 * the documentation module in one of its categories: one row per role and category, with one flag
 * per permission.
 *
 * @since 1.0.7
 */
@Entity
@Table(name = "INV_DOC_ROLE_PERM")
@Getter
@Setter
public class DocumentRolePermissionEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated database identifier. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_doc_role_perm_seq")
    @SequenceGenerator(
            name = "inv_doc_role_perm_seq",
            sequenceName = "INV_DOC_ROLE_PERM_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Role the permissions belong to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INV_ROLE_ID", nullable = false)
    private RoleEntity role;

    /** Category the permissions are set on; each role has at most one row per category. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOCUMENT_CATEGORY_ID", nullable = false)
    private DocumentCategoryEntity category;

    /** Whether the role can read the documents of the category. */
    @Column(name = "CAN_READ", nullable = false)
    private boolean canRead;

    /** Whether the role can upload documents to the category. */
    @Column(name = "CAN_UPLOAD", nullable = false)
    private boolean canUpload;

    /** Whether the role can edit the documents of the category. */
    @Column(name = "CAN_EDIT", nullable = false)
    private boolean canEdit;

    /** Whether the role can delete the documents of the category. */
    @Column(name = "CAN_DELETE", nullable = false)
    private boolean canDelete;
}
