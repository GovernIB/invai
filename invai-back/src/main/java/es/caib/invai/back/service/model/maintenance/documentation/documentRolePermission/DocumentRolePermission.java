package es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain model representing what a role of the INVAI application can do with the documents of the
 * documentation module in one of its categories.
 *
 * @since 1.0.7
 */
@Getter
@Setter
public class DocumentRolePermission {

    /** Unique database identifier key. */
    private Long id;

    /** Identifier of the role the permissions belong to. */
    private Long roleId;

    /** Identifier of the category the permissions are set on. */
    private Long categoryId;

    /** Name of the role, as Soffid reports it. */
    private String roleName;

    /** Description of the role, as Soffid reports it. */
    private String roleDescription;

    /** Whether the role can read the documents of the category. */
    private boolean canRead;

    /** Whether the role can upload documents to the category. */
    private boolean canUpload;

    /** Whether the role can edit the documents of the category. */
    private boolean canEdit;

    /** Whether the role can delete the documents of the category. */
    private boolean canDelete;

    /** Timestamp at which the record was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the record. */
    private String createdBy;
    /** Timestamp at which the record was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the record. */
    private String updatedBy;
    /** Timestamp at which the record was deleted; always {@code null}, the rows are deleted for good. */
    private LocalDateTime deletedAt;
    /** Username of the user who deleted the record; always {@code null}. */
    private String deletedBy;
}
