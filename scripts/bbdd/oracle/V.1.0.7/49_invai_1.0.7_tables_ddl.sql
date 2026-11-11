CREATE TABLE INV_DOC_ROLE_PERM
(
    ID                   NUMBER(19)        NOT NULL,
    INV_ROLE_ID          NUMBER(19)        NOT NULL,
    DOCUMENT_CATEGORY_ID NUMBER(19)        NOT NULL,
    CAN_READ             NUMBER(1) DEFAULT 0 NOT NULL,
    CAN_UPLOAD           NUMBER(1) DEFAULT 0 NOT NULL,
    CAN_EDIT             NUMBER(1) DEFAULT 0 NOT NULL,
    CAN_DELETE           NUMBER(1) DEFAULT 0 NOT NULL,
    CREATED_AT           TIMESTAMP(6)      NOT NULL,
    CREATED_BY           VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT           TIMESTAMP(6),
    UPDATED_BY           VARCHAR2(64 CHAR),
    DELETED_AT           TIMESTAMP(6),
    DELETED_BY           VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_DOC_ROLE_PERM IS 'What each role of the INVAI application can do with the documents of each category of the documentation module: one row per role and category, with one flag per permission. The rows of a role are created, with read permission only, when the role is added to the application, and the rows of a category when it is created, and they are deleted for good when the role is taken off';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.INV_ROLE_ID IS 'Role the permissions belong to (INV_ROLE)';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.DOCUMENT_CATEGORY_ID IS 'Category the permissions are set on (INV_DOCUMENT_CATEGORY, a record without a parent); at most one row per role and category';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.CAN_READ IS 'Whether the role can read documents (1) or not (0)';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.CAN_UPLOAD IS 'Whether the role can upload documents (1) or not (0)';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.CAN_EDIT IS 'Whether the role can edit documents (1) or not (0)';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.CAN_DELETE IS 'Whether the role can delete documents (1) or not (0)';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.DELETED_AT IS 'Never filled in: the rows are deleted for good, the deletion is recorded in INV_DOC_ROLE_PERM_AUD';
COMMENT ON COLUMN INV_DOC_ROLE_PERM.DELETED_BY IS 'Never filled in: the rows are deleted for good, the deletion is recorded in INV_DOC_ROLE_PERM_AUD';

CREATE TABLE INV_DOC_ROLE_PERM_AUD
(
    AUDIT_ID             NUMBER(19)        NOT NULL,
    DOC_ROLE_PERM_ID     NUMBER(19)        NOT NULL,
    INV_ROLE_ID          NUMBER(19)        NOT NULL,
    DOCUMENT_CATEGORY_ID NUMBER(19)        NOT NULL,
    CAN_READ             NUMBER(1)         NOT NULL,
    CAN_UPLOAD           NUMBER(1)         NOT NULL,
    CAN_EDIT             NUMBER(1)         NOT NULL,
    CAN_DELETE           NUMBER(1)         NOT NULL,
    CREATED_AT           TIMESTAMP(6),
    CREATED_BY           VARCHAR2(64 CHAR),
    UPDATED_AT           TIMESTAMP(6),
    UPDATED_BY           VARCHAR2(64 CHAR),
    DELETED_AT           TIMESTAMP(6),
    DELETED_BY           VARCHAR2(64 CHAR),
    AUD_ACTION           VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE           TIMESTAMP(6)      NOT NULL,
    AUDIT_USER           VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_DOC_ROLE_PERM_AUD IS 'Historical audit trail mirroring every INV_DOC_ROLE_PERM mutation (INSERT, UPDATE and DELETE)';
COMMENT ON COLUMN INV_DOC_ROLE_PERM_AUD.DOCUMENT_CATEGORY_ID IS 'Category the permissions were set on';

-- Sending by e-mail of the documentation
CREATE TABLE INV_APP_DOC_EMAIL
(
    ID                            NUMBER(19)          NOT NULL,
    APPLICATION_ID                NUMBER(19)          NOT NULL,
    SENDER_EMAIL                  VARCHAR2(320 CHAR)  NOT NULL,
    SUBJECT                       VARCHAR2(250 CHAR)  NOT NULL,
    MESSAGE                       VARCHAR2(4000 CHAR),
    CREATED_AT                    TIMESTAMP(6)        NOT NULL,
    CREATED_BY                    VARCHAR2(64 CHAR)   NOT NULL,
    UPDATED_AT                    TIMESTAMP(6),
    UPDATED_BY                    VARCHAR2(64 CHAR),
    DELETED_AT                    TIMESTAMP(6),
    DELETED_BY                    VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_DOC_EMAIL IS 'History of the e-mails sent from the Documentacio tab with attachments of an application; the record is created when the e-mail is sent and never changed or deleted';
COMMENT ON COLUMN INV_APP_DOC_EMAIL.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_APP_DOC_EMAIL.APPLICATION_ID IS 'Application the documentation was sent from (INV_APPLICATION)';
COMMENT ON COLUMN INV_APP_DOC_EMAIL.SENDER_EMAIL IS 'E-mail address of the user who sent it, used as the Reply-To of the message (the message is sent from the address of the application)';
COMMENT ON COLUMN INV_APP_DOC_EMAIL.SUBJECT IS 'Subject of the message';
COMMENT ON COLUMN INV_APP_DOC_EMAIL.MESSAGE IS 'Optional text the user wrote in the message';
COMMENT ON COLUMN INV_APP_DOC_EMAIL.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_DOC_EMAIL.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_DOC_EMAIL.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_DOC_EMAIL.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_DOC_EMAIL.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_DOC_EMAIL.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_DOC_EMAIL_RECIPIENT
(
    ID                            NUMBER(19)          NOT NULL,
    APP_DOC_EMAIL_ID              NUMBER(19)          NOT NULL,
    RECIPIENT_EMAIL               VARCHAR2(320 CHAR)  NOT NULL,
    CREATED_AT                    TIMESTAMP(6)        NOT NULL,
    CREATED_BY                    VARCHAR2(64 CHAR)   NOT NULL,
    UPDATED_AT                    TIMESTAMP(6),
    UPDATED_BY                    VARCHAR2(64 CHAR),
    DELETED_AT                    TIMESTAMP(6),
    DELETED_BY                    VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_DOC_EMAIL_RECIPIENT IS 'Recipients of a sent e-mail, one row per address';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_RECIPIENT.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_RECIPIENT.APP_DOC_EMAIL_ID IS 'Sent e-mail the address belongs to (INV_APP_DOC_EMAIL)';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_RECIPIENT.RECIPIENT_EMAIL IS 'E-mail address the message was sent to';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_RECIPIENT.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_RECIPIENT.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_RECIPIENT.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_RECIPIENT.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_RECIPIENT.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_RECIPIENT.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_DOC_EMAIL_ITEM
(
    ID                            NUMBER(19)          NOT NULL,
    APP_DOC_EMAIL_ID              NUMBER(19)          NOT NULL,
    APP_DOC_ATTACHMENT_VERSION_ID NUMBER(19)          NOT NULL,
    TITLE                         VARCHAR2(250 CHAR)  NOT NULL,
    VERSION_LABEL                 VARCHAR2(50 CHAR)   NOT NULL,
    FILE_NAME                     VARCHAR2(255 CHAR)  NOT NULL,
    CREATED_AT                    TIMESTAMP(6)        NOT NULL,
    CREATED_BY                    VARCHAR2(64 CHAR)   NOT NULL,
    UPDATED_AT                    TIMESTAMP(6),
    UPDATED_BY                    VARCHAR2(64 CHAR),
    DELETED_AT                    TIMESTAMP(6),
    DELETED_BY                    VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_DOC_EMAIL_ITEM IS 'Attachments (a specific version of a document) included in a sent e-mail; the text fields keep what was sent even if the document changes later';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_ITEM.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_ITEM.APP_DOC_EMAIL_ID IS 'Sent e-mail the item belongs to (INV_APP_DOC_EMAIL)';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_ITEM.APP_DOC_ATTACHMENT_VERSION_ID IS 'Version of the document that was attached (INV_APP_DOC_ATTACHMENT_VERSION)';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_ITEM.TITLE IS 'Title of the document when it was sent';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_ITEM.VERSION_LABEL IS 'Version label of the attached file when it was sent';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_ITEM.FILE_NAME IS 'Name of the attached file when it was sent';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_ITEM.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_ITEM.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_ITEM.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_ITEM.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_ITEM.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_DOC_EMAIL_ITEM.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';
