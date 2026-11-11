package es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission;

/**
 * Actions a role can be allowed to do with the documents and links of the documentation module.
 *
 * @since 1.0.7
 */
public enum DocumentPermission {

    /** See the documents and links, and download their files. */
    READ,

    /** Upload new documents, new versions of them, and new links. */
    UPLOAD,

    /** Edit the data of the documents and links. */
    EDIT,

    /** Delete documents, versions of them, and links. */
    DELETE
}
