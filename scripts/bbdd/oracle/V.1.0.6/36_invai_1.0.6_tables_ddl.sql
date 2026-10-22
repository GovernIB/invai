CREATE TABLE INV_DOCUMENT_CATEGORY
(
    ID         NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES    VARCHAR2(150 CHAR) NOT NULL,
    PARENT_ID  NUMBER(19),
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_DOCUMENT_CATEGORY IS 'Documentation categories and subcategories, in the same table: a category has no parent, a subcategory points to the category it belongs to (two levels at most, enforced in the facade)';
COMMENT ON COLUMN INV_DOCUMENT_CATEGORY.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_DOCUMENT_CATEGORY.NAME IS 'Name of the category or subcategory, Catalan';
COMMENT ON COLUMN INV_DOCUMENT_CATEGORY.NAME_ES IS 'Name of the category or subcategory, Spanish';
COMMENT ON COLUMN INV_DOCUMENT_CATEGORY.PARENT_ID IS 'Category this record belongs to (INV_DOCUMENT_CATEGORY); null when the record is itself a category';
COMMENT ON COLUMN INV_DOCUMENT_CATEGORY.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_DOCUMENT_CATEGORY.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_DOCUMENT_CATEGORY.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_DOCUMENT_CATEGORY.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_DOCUMENT_CATEGORY.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_DOCUMENT_CATEGORY.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_DOCUMENT_CATEGORY_AUD
(
    AUDIT_ID             NUMBER(19)         NOT NULL,
    DOCUMENT_CATEGORY_ID NUMBER(19)         NOT NULL,
    NAME                 VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES              VARCHAR2(150 CHAR) NOT NULL,
    PARENT_ID            NUMBER(19),
    CREATED_AT           TIMESTAMP(6),
    CREATED_BY           VARCHAR2(64 CHAR),
    UPDATED_AT           TIMESTAMP(6),
    UPDATED_BY           VARCHAR2(64 CHAR),
    DELETED_AT           TIMESTAMP(6),
    DELETED_BY           VARCHAR2(64 CHAR),
    AUD_ACTION           VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE           TIMESTAMP(6)       NOT NULL,
    AUDIT_USER           VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_DOCUMENT_CATEGORY_AUD IS 'Historical audit trail mirroring every INV_DOCUMENT_CATEGORY mutation';

CREATE TABLE INV_LKUP_APP_SECTION
(
    ID      NUMBER(19)        NOT NULL,
    CODE    VARCHAR2(30 CHAR) NOT NULL,
    NAME    VARCHAR2(64 CHAR) NOT NULL,
    NAME_ES VARCHAR2(64 CHAR) NOT NULL
);

COMMENT ON TABLE INV_LKUP_APP_SECTION IS 'Lookup table for the fixed sections (tabs) of an application a document or link can be filed under, Catalan/Spanish names';
COMMENT ON COLUMN INV_LKUP_APP_SECTION.ID IS 'Unique surrogate key for the section entry';
COMMENT ON COLUMN INV_LKUP_APP_SECTION.CODE IS 'Fixed technical code for the section (e.g. SECURITY)';
COMMENT ON COLUMN INV_LKUP_APP_SECTION.NAME IS 'Display label of the section, Catalan';
COMMENT ON COLUMN INV_LKUP_APP_SECTION.NAME_ES IS 'Display label of the section, Spanish';

CREATE TABLE INV_LKUP_DOCUMENT_STATUS
(
    ID      NUMBER(19)        NOT NULL,
    CODE    VARCHAR2(30 CHAR) NOT NULL,
    NAME    VARCHAR2(64 CHAR) NOT NULL,
    NAME_ES VARCHAR2(64 CHAR) NOT NULL
);

COMMENT ON TABLE INV_LKUP_DOCUMENT_STATUS IS 'Lookup table for the fixed document states (ACTIVE/IN_REVIEW/OBSOLETE), Catalan/Spanish names';
COMMENT ON COLUMN INV_LKUP_DOCUMENT_STATUS.ID IS 'Unique surrogate key for the document status entry';
COMMENT ON COLUMN INV_LKUP_DOCUMENT_STATUS.CODE IS 'Fixed technical code for the state (e.g. ACTIVE)';
COMMENT ON COLUMN INV_LKUP_DOCUMENT_STATUS.NAME IS 'Display label of the state, Catalan';
COMMENT ON COLUMN INV_LKUP_DOCUMENT_STATUS.NAME_ES IS 'Display label of the state, Spanish';

CREATE TABLE INV_LKUP_MIME_TYPE
(
    ID        NUMBER(19)         NOT NULL,
    EXTENSION VARCHAR2(10 CHAR)  NOT NULL,
    MIME_TYPE VARCHAR2(150 CHAR) NOT NULL,
    NAME      VARCHAR2(64 CHAR)  NOT NULL
);

COMMENT ON TABLE INV_LKUP_MIME_TYPE IS 'Lookup table for the file formats a document can be uploaded in, one row per file extension; an upload is accepted only if its extension is listed here';
COMMENT ON COLUMN INV_LKUP_MIME_TYPE.ID IS 'Unique surrogate key for the file format entry';
COMMENT ON COLUMN INV_LKUP_MIME_TYPE.EXTENSION IS 'File extension in lower case and without the dot (e.g. pdf), unique';
COMMENT ON COLUMN INV_LKUP_MIME_TYPE.MIME_TYPE IS 'Media type matching the extension (e.g. application/pdf); several extensions can share one (jpg and jpeg)';
COMMENT ON COLUMN INV_LKUP_MIME_TYPE.NAME IS 'Display label of the format shown as the document type (e.g. PDF), not translated';

CREATE TABLE INV_APP_DOC_ATTACHMENT
(
    ID                   NUMBER(19)         NOT NULL,
    APPLICATION_ID       NUMBER(19)         NOT NULL,
    TITLE                VARCHAR2(250 CHAR) NOT NULL,
    SECTION_ID           NUMBER(19)         NOT NULL,
    DOCUMENT_CATEGORY_ID NUMBER(19)         NOT NULL,
    STATUS_ID            NUMBER(19)         NOT NULL,
    CREATED_AT           TIMESTAMP(6)       NOT NULL,
    CREATED_BY           VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT           TIMESTAMP(6),
    UPDATED_BY           VARCHAR2(64 CHAR),
    DELETED_AT           TIMESTAMP(6),
    DELETED_BY           VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_DOC_ATTACHMENT IS 'Document attached to an application ("Adjunts" list of the Documentacio tab); the uploaded files live in its attachments';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT.APPLICATION_ID IS 'Owning application (INV_APPLICATION)';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT.TITLE IS 'Title of the document';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT.SECTION_ID IS 'Section of the application the document belongs to (INV_LKUP_APP_SECTION)';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT.DOCUMENT_CATEGORY_ID IS 'Most specific category of the document, either a category or a subcategory (INV_DOCUMENT_CATEGORY); the parent category is derived from it; mandatory';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT.STATUS_ID IS 'Current state of the document (INV_LKUP_DOCUMENT_STATUS)';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_DOC_ATT_AUD
(
    AUDIT_ID             NUMBER(19)         NOT NULL,
    APP_DOC_ATTACHMENT_ID    NUMBER(19)         NOT NULL,
    APPLICATION_ID       NUMBER(19)         NOT NULL,
    TITLE                VARCHAR2(250 CHAR) NOT NULL,
    SECTION_ID           NUMBER(19)         NOT NULL,
    DOCUMENT_CATEGORY_ID NUMBER(19)         NOT NULL,
    STATUS_ID            NUMBER(19)         NOT NULL,
    CREATED_AT           TIMESTAMP(6),
    CREATED_BY           VARCHAR2(64 CHAR),
    UPDATED_AT           TIMESTAMP(6),
    UPDATED_BY           VARCHAR2(64 CHAR),
    DELETED_AT           TIMESTAMP(6),
    DELETED_BY           VARCHAR2(64 CHAR),
    AUD_ACTION           VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE           TIMESTAMP(6)       NOT NULL,
    AUDIT_USER           VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_DOC_ATT_AUD IS 'Historical audit trail mirroring every INV_APP_DOC_ATTACHMENT mutation';

CREATE TABLE INV_APP_DOC_ATTACHMENT_VERSION
(
    ID                NUMBER(19)         NOT NULL,
    APP_DOC_ATTACHMENT_ID NUMBER(19)         NOT NULL,
    VERSION_LABEL     VARCHAR2(50 CHAR)  NOT NULL,
    FILE_NAME         VARCHAR2(255 CHAR) NOT NULL,
    MIME_TYPE_ID      NUMBER(19)         NOT NULL,
    FILE_SIZE         NUMBER(19)         NOT NULL,
    CHECKSUM_SHA256   VARCHAR2(64 CHAR)  NOT NULL,
    IS_CURRENT        NUMBER(1)          DEFAULT 0 NOT NULL,
    CHANGE_NOTE       VARCHAR2(500 CHAR),
    CONTENT           BLOB               NOT NULL,
    CREATED_AT        TIMESTAMP(6)       NOT NULL,
    CREATED_BY        VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT        TIMESTAMP(6),
    UPDATED_BY        VARCHAR2(64 CHAR),
    DELETED_AT        TIMESTAMP(6),
    DELETED_BY        VARCHAR2(64 CHAR)
)
    LOB (CONTENT) STORE AS SECUREFILE (TABLESPACE INVAI_LOB NOCACHE);

COMMENT ON TABLE INV_APP_DOC_ATTACHMENT_VERSION IS 'Each file uploaded for a document (detail of the "Adjunts" list), content included; listings of attachments must not select CONTENT so the BLOB is never read';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.APP_DOC_ATTACHMENT_ID IS 'Document this file is a version of (INV_APP_DOC_ATTACHMENT)';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.VERSION_LABEL IS 'Version typed in by hand by whoever uploads the file (e.g. 1.0, 2.1)';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.FILE_NAME IS 'Original name of the uploaded file';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.MIME_TYPE_ID IS 'File format of the uploaded file (INV_LKUP_MIME_TYPE), chosen from its extension; its name is the document type shown in the list';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.FILE_SIZE IS 'Size of the uploaded file in bytes (the UI shows it in KB)';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.CHECKSUM_SHA256 IS 'Hexadecimal SHA-256 of the uploaded content, to detect corruption and re-uploads of the same file';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.IS_CURRENT IS 'Whether this is the current version of the document (1) or not (0); at most one non-deleted version per document is current, enforced by a unique index';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.CHANGE_NOTE IS 'Optional note describing what changed in this version';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.CONTENT IS 'Bytes of the uploaded file; immutable, so it is not mirrored in INV_APP_DOC_ATT_VER_AUD';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.CREATED_AT IS 'Audit timestamp registering record creation, i.e. the upload date';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.CREATED_BY IS 'Audit identifier of the user that created the record, i.e. who uploaded the file';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_DOC_ATTACHMENT_VERSION.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_DOC_ATT_VER_AUD
(
    AUDIT_ID                  NUMBER(19)         NOT NULL,
    APP_DOC_ATTACHMENT_VERSION_ID NUMBER(19)         NOT NULL,
    APP_DOC_ATTACHMENT_ID         NUMBER(19)         NOT NULL,
    VERSION_LABEL             VARCHAR2(50 CHAR)  NOT NULL,
    FILE_NAME                 VARCHAR2(255 CHAR) NOT NULL,
    MIME_TYPE_ID              NUMBER(19)         NOT NULL,
    FILE_SIZE                 NUMBER(19)         NOT NULL,
    CHECKSUM_SHA256           VARCHAR2(64 CHAR)  NOT NULL,
    IS_CURRENT                NUMBER(1),
    CHANGE_NOTE               VARCHAR2(500 CHAR),
    CREATED_AT                TIMESTAMP(6),
    CREATED_BY                VARCHAR2(64 CHAR),
    UPDATED_AT                TIMESTAMP(6),
    UPDATED_BY                VARCHAR2(64 CHAR),
    DELETED_AT                TIMESTAMP(6),
    DELETED_BY                VARCHAR2(64 CHAR),
    AUD_ACTION                VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE                TIMESTAMP(6)       NOT NULL,
    AUDIT_USER                VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_DOC_ATT_VER_AUD IS 'Historical audit trail mirroring every INV_APP_DOC_ATTACHMENT_VERSION mutation (never the file content), plus one row per download (action DOWNLOAD) or view in the browser (action VIEW) of its file';

CREATE TABLE INV_APP_LINK
(
    ID             NUMBER(19)          NOT NULL,
    APPLICATION_ID NUMBER(19)          NOT NULL,
    TITLE          VARCHAR2(250 CHAR)  NOT NULL,
    DESCRIPTION    VARCHAR2(1000 CHAR),
    SECTION_ID     NUMBER(19)          NOT NULL,
    URL            VARCHAR2(500 CHAR)  NOT NULL,
    CREATED_AT     TIMESTAMP(6)        NOT NULL,
    CREATED_BY     VARCHAR2(64 CHAR)   NOT NULL,
    UPDATED_AT     TIMESTAMP(6),
    UPDATED_BY     VARCHAR2(64 CHAR),
    DELETED_AT     TIMESTAMP(6),
    DELETED_BY     VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_LINK IS 'Link of an application ("Enllacos" list of the Documentacio tab)';
COMMENT ON COLUMN INV_APP_LINK.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_APP_LINK.APPLICATION_ID IS 'Owning application (INV_APPLICATION)';
COMMENT ON COLUMN INV_APP_LINK.TITLE IS 'Title of the link';
COMMENT ON COLUMN INV_APP_LINK.DESCRIPTION IS 'Optional description of the link';
COMMENT ON COLUMN INV_APP_LINK.SECTION_ID IS 'Section of the application the link belongs to (INV_LKUP_APP_SECTION)';
COMMENT ON COLUMN INV_APP_LINK.URL IS 'Address the link points to';
COMMENT ON COLUMN INV_APP_LINK.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_LINK.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_LINK.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_LINK.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_LINK.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_LINK.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_LINK_AUD
(
    AUDIT_ID       NUMBER(19)         NOT NULL,
    APP_LINK_ID    NUMBER(19)         NOT NULL,
    APPLICATION_ID NUMBER(19)         NOT NULL,
    TITLE          VARCHAR2(250 CHAR) NOT NULL,
    DESCRIPTION    VARCHAR2(1000 CHAR),
    SECTION_ID     NUMBER(19)         NOT NULL,
    URL            VARCHAR2(500 CHAR) NOT NULL,
    CREATED_AT     TIMESTAMP(6),
    CREATED_BY     VARCHAR2(64 CHAR),
    UPDATED_AT     TIMESTAMP(6),
    UPDATED_BY     VARCHAR2(64 CHAR),
    DELETED_AT     TIMESTAMP(6),
    DELETED_BY     VARCHAR2(64 CHAR),
    AUD_ACTION     VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE     TIMESTAMP(6)       NOT NULL,
    AUDIT_USER     VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_LINK_AUD IS 'Historical audit trail mirroring every INV_APP_LINK mutation';

CREATE TABLE INV_APP_DOCUMENTATION_METADATA
(
    ID                              NUMBER(19)           NOT NULL,
    APP_DOC_ATTACHMENT_VERSION_ID NUMBER(19)           NOT NULL,
    META_KEY                        VARCHAR2(80 CHAR)    NOT NULL,
    META_VALUE                      VARCHAR2(1024 CHAR)  NOT NULL,
    CREATED_AT                      TIMESTAMP(6)         NOT NULL,
    CREATED_BY                      VARCHAR2(64 CHAR)    NOT NULL,
    UPDATED_AT                      TIMESTAMP(6),
    UPDATED_BY                      VARCHAR2(64 CHAR),
    DELETED_AT                      TIMESTAMP(6),
    DELETED_BY                      VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_DOCUMENTATION_METADATA IS 'Extensible key-value metadata of an attachment of a document (every document has at least one), so new metadata needs no change of schema';
COMMENT ON COLUMN INV_APP_DOCUMENTATION_METADATA.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_APP_DOCUMENTATION_METADATA.APP_DOC_ATTACHMENT_VERSION_ID IS 'Attachment the metadata belongs to (INV_APP_DOC_ATTACHMENT_VERSION)';
COMMENT ON COLUMN INV_APP_DOCUMENTATION_METADATA.META_KEY IS 'Key of the metadata (e.g. eni.claseDocumento), unique among the active metadata of the same attachment';
COMMENT ON COLUMN INV_APP_DOCUMENTATION_METADATA.META_VALUE IS 'Value of the metadata, free text';
COMMENT ON COLUMN INV_APP_DOCUMENTATION_METADATA.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_DOCUMENTATION_METADATA.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_DOCUMENTATION_METADATA.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_DOCUMENTATION_METADATA.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_DOCUMENTATION_METADATA.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_DOCUMENTATION_METADATA.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_DOC_META_AUD
(
    AUDIT_ID                        NUMBER(19)           NOT NULL,
    APP_DOCUMENTATION_METADATA_ID   NUMBER(19)           NOT NULL,
    APP_DOC_ATTACHMENT_VERSION_ID NUMBER(19)           NOT NULL,
    META_KEY                        VARCHAR2(80 CHAR)    NOT NULL,
    META_VALUE                      VARCHAR2(1024 CHAR)  NOT NULL,
    CREATED_AT                      TIMESTAMP(6),
    CREATED_BY                      VARCHAR2(64 CHAR),
    UPDATED_AT                      TIMESTAMP(6),
    UPDATED_BY                      VARCHAR2(64 CHAR),
    DELETED_AT                      TIMESTAMP(6),
    DELETED_BY                      VARCHAR2(64 CHAR),
    AUD_ACTION                      VARCHAR2(10 CHAR)    NOT NULL,
    AUDIT_DATE                      TIMESTAMP(6)         NOT NULL,
    AUDIT_USER                      VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_DOC_META_AUD IS 'Historical audit trail mirroring every INV_APP_DOCUMENTATION_METADATA mutation';
