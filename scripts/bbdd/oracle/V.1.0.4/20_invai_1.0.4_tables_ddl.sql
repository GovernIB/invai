CREATE TABLE INV_IDENTITY_PROVIDER
(
    ID         NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(150 CHAR) NOT NULL,
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_IDENTITY_PROVIDER IS 'Catalog table for identity providers within the Manteniments / Seguretat module';
COMMENT ON COLUMN INV_IDENTITY_PROVIDER.ID IS 'Unique identifier for the identity provider';
COMMENT ON COLUMN INV_IDENTITY_PROVIDER.NAME IS 'Identity provider descriptive label (e.g. "Cl@ve")';
COMMENT ON COLUMN INV_IDENTITY_PROVIDER.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_IDENTITY_PROVIDER.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_IDENTITY_PROVIDER.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_IDENTITY_PROVIDER.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_IDENTITY_PROVIDER.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_IDENTITY_PROVIDER.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_IDENTITY_PROVIDER_AUD
(
    AUDIT_ID            NUMBER(19)         NOT NULL,
    IDENTITY_PROVIDER_ID NUMBER(19)        NOT NULL,
    NAME                VARCHAR2(150 CHAR) NOT NULL,
    CREATED_AT          TIMESTAMP(6),
    CREATED_BY          VARCHAR2(64 CHAR),
    UPDATED_AT          TIMESTAMP(6),
    UPDATED_BY          VARCHAR2(64 CHAR),
    DELETED_AT          TIMESTAMP(6),
    DELETED_BY          VARCHAR2(64 CHAR),
    AUD_ACTION          VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE          TIMESTAMP(6)       NOT NULL,
    AUDIT_USER          VARCHAR2(64 CHAR)
);

CREATE TABLE INV_PERSONAL_DATA_PROCESSING
(
    ID         NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES    VARCHAR2(100 CHAR),
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_PERSONAL_DATA_PROCESSING IS 'Catalog table for personal data processing types within the Manteniments / Seguretat module';
COMMENT ON COLUMN INV_PERSONAL_DATA_PROCESSING.ID IS 'Unique identifier for the personal data processing entry';
COMMENT ON COLUMN INV_PERSONAL_DATA_PROCESSING.NAME IS 'Personal data processing descriptive label';
COMMENT ON COLUMN INV_PERSONAL_DATA_PROCESSING.NAME_ES IS 'Spanish translation descriptive name for the personal data processing entry';
COMMENT ON COLUMN INV_PERSONAL_DATA_PROCESSING.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_PERSONAL_DATA_PROCESSING.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_PERSONAL_DATA_PROCESSING.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_PERSONAL_DATA_PROCESSING.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_PERSONAL_DATA_PROCESSING.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_PERSONAL_DATA_PROCESSING.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_PERS_DATA_PROC_AUD
(
    AUDIT_ID                     NUMBER(19)         NOT NULL,
    PERSONAL_DATA_PROCESSING_ID  NUMBER(19)         NOT NULL,
    NAME                         VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES                      VARCHAR2(100 CHAR),
    CREATED_AT                   TIMESTAMP(6),
    CREATED_BY                   VARCHAR2(64 CHAR),
    UPDATED_AT                   TIMESTAMP(6),
    UPDATED_BY                   VARCHAR2(64 CHAR),
    DELETED_AT                   TIMESTAMP(6),
    DELETED_BY                   VARCHAR2(64 CHAR),
    AUD_ACTION                   VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE                   TIMESTAMP(6)       NOT NULL,
    AUDIT_USER                   VARCHAR2(64 CHAR)
);

CREATE TABLE INV_WEB_CONTEXT
(
    ID         NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES    VARCHAR2(100 CHAR),
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_WEB_CONTEXT IS 'Catalog table for web contexts within the Manteniments / Seguretat module';
COMMENT ON COLUMN INV_WEB_CONTEXT.ID IS 'Unique identifier for the web context';
COMMENT ON COLUMN INV_WEB_CONTEXT.NAME IS 'Web context descriptive label';
COMMENT ON COLUMN INV_WEB_CONTEXT.NAME_ES IS 'Spanish translation descriptive name for the web context';
COMMENT ON COLUMN INV_WEB_CONTEXT.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_WEB_CONTEXT.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_WEB_CONTEXT.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_WEB_CONTEXT.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_WEB_CONTEXT.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_WEB_CONTEXT.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_WEB_CONTEXT_AUD
(
    AUDIT_ID        NUMBER(19)         NOT NULL,
    WEB_CONTEXT_ID  NUMBER(19)         NOT NULL,
    NAME            VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES         VARCHAR2(100 CHAR),
    CREATED_AT      TIMESTAMP(6),
    CREATED_BY      VARCHAR2(64 CHAR),
    UPDATED_AT      TIMESTAMP(6),
    UPDATED_BY      VARCHAR2(64 CHAR),
    DELETED_AT      TIMESTAMP(6),
    DELETED_BY      VARCHAR2(64 CHAR),
    AUD_ACTION      VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE      TIMESTAMP(6)       NOT NULL,
    AUDIT_USER      VARCHAR2(64 CHAR)
);

CREATE TABLE INV_LKUP_SECURITY_LEVEL
(
    ID      NUMBER(19)         NOT NULL,
    NAME    VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES VARCHAR2(100 CHAR) NOT NULL
);

COMMENT ON TABLE INV_LKUP_SECURITY_LEVEL IS 'Lookup table for ENS security levels assignable to an application within the Seguretat module';
COMMENT ON COLUMN INV_LKUP_SECURITY_LEVEL.ID IS 'Unique identifier for the security level';
COMMENT ON COLUMN INV_LKUP_SECURITY_LEVEL.NAME IS 'Security level descriptive label (e.g. "Alt")';
COMMENT ON COLUMN INV_LKUP_SECURITY_LEVEL.NAME_ES IS 'Spanish translation descriptive name for the security level';

CREATE TABLE INV_LKUP_ENS_SUBJECT
(
    ID      NUMBER(19)         NOT NULL,
    NAME    VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES VARCHAR2(100 CHAR) NOT NULL
);

COMMENT ON TABLE INV_LKUP_ENS_SUBJECT IS 'Lookup table for ENS subjection statuses assignable to an application within the Seguretat module';
COMMENT ON COLUMN INV_LKUP_ENS_SUBJECT.ID IS 'Unique identifier for the ENS subjection status';
COMMENT ON COLUMN INV_LKUP_ENS_SUBJECT.NAME IS 'ENS subjection descriptive label (e.g. "Sí")';
COMMENT ON COLUMN INV_LKUP_ENS_SUBJECT.NAME_ES IS 'Spanish translation descriptive name for the ENS subjection status';

CREATE TABLE INV_SECURITY_MEASURE_TYPE
(
    ID         NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES    VARCHAR2(100 CHAR),
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_SECURITY_MEASURE_TYPE IS 'Catalog table for security measure types (e.g. organizational, technical, procedural) within the Manteniments / Seguretat module';
COMMENT ON COLUMN INV_SECURITY_MEASURE_TYPE.ID IS 'Unique identifier for the security measure type';
COMMENT ON COLUMN INV_SECURITY_MEASURE_TYPE.NAME IS 'Security measure type descriptive label';
COMMENT ON COLUMN INV_SECURITY_MEASURE_TYPE.NAME_ES IS 'Spanish translation descriptive name for the security measure type';
COMMENT ON COLUMN INV_SECURITY_MEASURE_TYPE.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_SECURITY_MEASURE_TYPE.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_SECURITY_MEASURE_TYPE.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_SECURITY_MEASURE_TYPE.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_SECURITY_MEASURE_TYPE.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_SECURITY_MEASURE_TYPE.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_SECURITY_MEASURE_TYPE_AUD
(
    AUDIT_ID               NUMBER(19)         NOT NULL,
    SECURITY_MEASURE_TYPE_ID NUMBER(19)       NOT NULL,
    NAME                   VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES                VARCHAR2(100 CHAR),
    CREATED_AT             TIMESTAMP(6),
    CREATED_BY             VARCHAR2(64 CHAR),
    UPDATED_AT             TIMESTAMP(6),
    UPDATED_BY             VARCHAR2(64 CHAR),
    DELETED_AT             TIMESTAMP(6),
    DELETED_BY             VARCHAR2(64 CHAR),
    AUD_ACTION             VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE             TIMESTAMP(6)       NOT NULL,
    AUDIT_USER             VARCHAR2(64 CHAR)
);

CREATE TABLE INV_ENS_REQUIREMENT
(
    ID         NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES    VARCHAR2(100 CHAR),
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_ENS_REQUIREMENT IS 'Catalog table for ENS (Esquema Nacional de Seguridad) requirements within the Manteniments / Seguretat module';
COMMENT ON COLUMN INV_ENS_REQUIREMENT.ID IS 'Unique identifier for the ENS requirement';
COMMENT ON COLUMN INV_ENS_REQUIREMENT.NAME IS 'ENS requirement descriptive label';
COMMENT ON COLUMN INV_ENS_REQUIREMENT.NAME_ES IS 'Spanish translation descriptive name for the ENS requirement';
COMMENT ON COLUMN INV_ENS_REQUIREMENT.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_ENS_REQUIREMENT.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_ENS_REQUIREMENT.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_ENS_REQUIREMENT.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_ENS_REQUIREMENT.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_ENS_REQUIREMENT.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_ENS_REQUIREMENT_AUD
(
    AUDIT_ID            NUMBER(19)         NOT NULL,
    ENS_REQUIREMENT_ID  NUMBER(19)         NOT NULL,
    NAME                VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES             VARCHAR2(100 CHAR),
    CREATED_AT          TIMESTAMP(6),
    CREATED_BY          VARCHAR2(64 CHAR),
    UPDATED_AT          TIMESTAMP(6),
    UPDATED_BY          VARCHAR2(64 CHAR),
    DELETED_AT          TIMESTAMP(6),
    DELETED_BY          VARCHAR2(64 CHAR),
    AUD_ACTION          VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE          TIMESTAMP(6)       NOT NULL,
    AUDIT_USER          VARCHAR2(64 CHAR)
);

CREATE TABLE INV_APP_SECURITY
(
    ID             NUMBER(19)        NOT NULL,
    APPLICATION_ID NUMBER(19)        NOT NULL,
    OBSERVATION    CLOB,
    CREATED_AT     TIMESTAMP(6)      NOT NULL,
    CREATED_BY     VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT     TIMESTAMP(6),
    UPDATED_BY     VARCHAR2(64 CHAR),
    DELETED_AT     TIMESTAMP(6),
    DELETED_BY     VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_SECURITY IS 'Anchor row binding the "Seguretat" tab to a corporate application, mirroring INV_APP_INFORMATION_SYSTEM_DB/INV_APP_DEVELOPMENT/INV_APP_RESPONSIBLE_AUTHORIZED: one row per application, to be referenced by the Seguretat tab child tables in a later phase';
COMMENT ON COLUMN INV_APP_SECURITY.ID IS 'Unique security anchor tracking primary key';
COMMENT ON COLUMN INV_APP_SECURITY.APPLICATION_ID IS 'Foreign key referencing the parent corporate application';
COMMENT ON COLUMN INV_APP_SECURITY.OBSERVATION IS 'Additional comments or observations';
COMMENT ON COLUMN INV_APP_SECURITY.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_SECURITY.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_SECURITY.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_SECURITY.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_SECURITY.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_SECURITY.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_SECURITY_AUD
(
    AUDIT_ID       NUMBER(19)        NOT NULL,
    ID             NUMBER(19)        NOT NULL,
    APPLICATION_ID NUMBER(19)        NOT NULL,
    OBSERVATION    CLOB,
    CREATED_AT     TIMESTAMP(6),
    CREATED_BY     VARCHAR2(64 CHAR),
    UPDATED_AT     TIMESTAMP(6),
    UPDATED_BY     VARCHAR2(64 CHAR),
    DELETED_AT     TIMESTAMP(6),
    DELETED_BY     VARCHAR2(64 CHAR),
    AUD_ACTION     VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE     TIMESTAMP(6)      NOT NULL,
    AUDIT_USER     VARCHAR2(64 CHAR)
);

CREATE TABLE INV_SECURITY_ROLE
(
    ID          NUMBER(19)         NOT NULL,
    ROLE_ID     NUMBER(19)         NOT NULL,
    NAME        VARCHAR2(150 CHAR) NOT NULL,
    SYSTEM      VARCHAR2(150 CHAR),
    DESCRIPTION VARCHAR2(500 CHAR),
    CREATED_AT  TIMESTAMP(6)       NOT NULL,
    CREATED_BY  VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT  TIMESTAMP(6),
    UPDATED_BY  VARCHAR2(64 CHAR),
    DELETED_AT  TIMESTAMP(6),
    DELETED_BY  VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_SECURITY_ROLE IS 'Catalog table for application security roles within the Manteniments / Seguretat module. Named SECURITY_ROLE (rather than ROLE) to avoid colliding with the unrelated development role catalog (INV_ROLE). Locally cached copy of a Soffid role (ROLE_ID/NAME/SYSTEM/DESCRIPTION from GET scim2/v1/Role/); currently only reachable via the Soffid search proxy, no local CRUD - to be resolved/created from the external Soffid identity system in a future iteration';
COMMENT ON COLUMN INV_SECURITY_ROLE.ID IS 'Unique identifier for the security role';
COMMENT ON COLUMN INV_SECURITY_ROLE.ROLE_ID IS 'Soffid role id (as provided by GET scim2/v1/Role/)';
COMMENT ON COLUMN INV_SECURITY_ROLE.NAME IS 'Security role descriptive label, as copied from Soffid';
COMMENT ON COLUMN INV_SECURITY_ROLE.SYSTEM IS 'Soffid system (environment) the role is associated with, as copied from Soffid';
COMMENT ON COLUMN INV_SECURITY_ROLE.DESCRIPTION IS 'Human-readable role description, as copied from Soffid';
COMMENT ON COLUMN INV_SECURITY_ROLE.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_SECURITY_ROLE.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_SECURITY_ROLE.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_SECURITY_ROLE.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_SECURITY_ROLE.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_SECURITY_ROLE.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_ROLE
(
    ID               NUMBER(19)        NOT NULL,
    APP_SECURITY_ID  NUMBER(19)        NOT NULL,
    SECURITY_ROLE_ID NUMBER(19)        NOT NULL,
    CREATED_AT       TIMESTAMP(6)      NOT NULL,
    CREATED_BY       VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT       TIMESTAMP(6),
    UPDATED_BY       VARCHAR2(64 CHAR),
    DELETED_AT       TIMESTAMP(6),
    DELETED_BY       VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_ROLE IS 'Roles de seguretat assignats a una aplicacio, penjant de l''ancoratge INV_APP_SECURITY';
COMMENT ON COLUMN INV_APP_ROLE.ID IS 'Unique application role tracking primary key';
COMMENT ON COLUMN INV_APP_ROLE.APP_SECURITY_ID IS 'Foreign key referencing the parent security anchor';
COMMENT ON COLUMN INV_APP_ROLE.SECURITY_ROLE_ID IS 'Foreign key referencing the assigned security role catalog entry';
COMMENT ON COLUMN INV_APP_ROLE.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_ROLE.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_ROLE.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_ROLE.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_ROLE.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_ROLE.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_ROLE_AUD
(
    AUDIT_ID         NUMBER(19)        NOT NULL,
    APP_ROLE_ID      NUMBER(19)        NOT NULL,
    APP_SECURITY_ID  NUMBER(19)        NOT NULL,
    SECURITY_ROLE_ID NUMBER(19)        NOT NULL,
    CREATED_AT       TIMESTAMP(6),
    CREATED_BY       VARCHAR2(64 CHAR),
    UPDATED_AT       TIMESTAMP(6),
    UPDATED_BY       VARCHAR2(64 CHAR),
    DELETED_AT       TIMESTAMP(6),
    DELETED_BY       VARCHAR2(64 CHAR),
    AUD_ACTION       VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE       TIMESTAMP(6)      NOT NULL,
    AUDIT_USER       VARCHAR2(64 CHAR)
);

CREATE TABLE INV_APP_WEB_CONTEXT
(
    ID              NUMBER(19)        NOT NULL,
    APP_SECURITY_ID NUMBER(19)        NOT NULL,
    WEB_CONTEXT_ID  NUMBER(19)        NOT NULL,
    FIELD_ID        NUMBER(19)        NOT NULL,
    OBSERVATION     CLOB,
    CREATED_AT      TIMESTAMP(6)      NOT NULL,
    CREATED_BY      VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT      TIMESTAMP(6),
    UPDATED_BY      VARCHAR2(64 CHAR),
    DELETED_AT      TIMESTAMP(6),
    DELETED_BY      VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_WEB_CONTEXT IS 'Contextos web assignats a una aplicacio, penjant de l''ancoratge INV_APP_SECURITY';
COMMENT ON COLUMN INV_APP_WEB_CONTEXT.ID IS 'Unique application web context tracking primary key';
COMMENT ON COLUMN INV_APP_WEB_CONTEXT.APP_SECURITY_ID IS 'Foreign key referencing the parent security anchor';
COMMENT ON COLUMN INV_APP_WEB_CONTEXT.WEB_CONTEXT_ID IS 'Foreign key referencing the assigned web context catalog entry';
COMMENT ON COLUMN INV_APP_WEB_CONTEXT.FIELD_ID IS 'Foreign key referencing the functional field/scope catalog entry';
COMMENT ON COLUMN INV_APP_WEB_CONTEXT.OBSERVATION IS 'Additional comments or observations';
COMMENT ON COLUMN INV_APP_WEB_CONTEXT.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_WEB_CONTEXT.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_WEB_CONTEXT.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_WEB_CONTEXT.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_WEB_CONTEXT.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_WEB_CONTEXT.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_WEB_CONTEXT_AUD
(
    AUDIT_ID            NUMBER(19)        NOT NULL,
    APP_WEB_CONTEXT_ID  NUMBER(19)        NOT NULL,
    APP_SECURITY_ID     NUMBER(19)        NOT NULL,
    WEB_CONTEXT_ID      NUMBER(19)        NOT NULL,
    FIELD_ID            NUMBER(19)        NOT NULL,
    OBSERVATION         CLOB,
    CREATED_AT          TIMESTAMP(6),
    CREATED_BY          VARCHAR2(64 CHAR),
    UPDATED_AT          TIMESTAMP(6),
    UPDATED_BY          VARCHAR2(64 CHAR),
    DELETED_AT          TIMESTAMP(6),
    DELETED_BY          VARCHAR2(64 CHAR),
    AUD_ACTION          VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE          TIMESTAMP(6)      NOT NULL,
    AUDIT_USER          VARCHAR2(64 CHAR)
);

CREATE TABLE INV_APP_ENS_CLASSIFICATION
(
    ID                          NUMBER(19)        NOT NULL,
    APP_SECURITY_ID             NUMBER(19)        NOT NULL,
    OVERALL_GRADE_LEVEL_ID      NUMBER(19),
    IDENTITY_PROVIDER_ID        NUMBER(19),
    ENS_SUBJECT_ID              NUMBER(19),
    PERSONAL_DATA_PROCESSING_ID NUMBER(19),
    APPROVAL_DATE               TIMESTAMP(6),
    CONFIDENTIALITY_LEVEL_ID    NUMBER(19),
    INTEGRITY_LEVEL_ID          NUMBER(19),
    TRACEABILITY_LEVEL_ID       NUMBER(19),
    AVAILABILITY_LEVEL_ID       NUMBER(19),
    AUTHENTICITY_LEVEL_ID       NUMBER(19),
    CREATED_AT                  TIMESTAMP(6)      NOT NULL,
    CREATED_BY                  VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT                  TIMESTAMP(6),
    UPDATED_BY                  VARCHAR2(64 CHAR),
    DELETED_AT                  TIMESTAMP(6),
    DELETED_BY                  VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_ENS_CLASSIFICATION IS 'Classificacio ENS d''una aplicacio, penjant de l''ancoratge INV_APP_SECURITY';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.ID IS 'Unique ENS classification tracking primary key';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.APP_SECURITY_ID IS 'Foreign key referencing the parent security anchor';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.OVERALL_GRADE_LEVEL_ID IS 'Optional foreign key referencing the security level lookup entry for the calculated overall ENS grade';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.IDENTITY_PROVIDER_ID IS 'Optional foreign key referencing the identity provider catalog entry';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.ENS_SUBJECT_ID IS 'Optional foreign key referencing the ENS subjection lookup entry';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.PERSONAL_DATA_PROCESSING_ID IS 'Optional foreign key referencing the personal data processing catalog entry';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.APPROVAL_DATE IS 'Approval date of the ENS classification';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.CONFIDENTIALITY_LEVEL_ID IS 'Optional foreign key referencing the security level lookup entry for confidentiality';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.INTEGRITY_LEVEL_ID IS 'Optional foreign key referencing the security level lookup entry for integrity';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.TRACEABILITY_LEVEL_ID IS 'Optional foreign key referencing the security level lookup entry for traceability';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.AVAILABILITY_LEVEL_ID IS 'Optional foreign key referencing the security level lookup entry for availability';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.AUTHENTICITY_LEVEL_ID IS 'Optional foreign key referencing the security level lookup entry for authenticity';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_ENS_CLASSIFICATION.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_ENS_CLASSIFICATION_AUD
(
    AUDIT_ID                     NUMBER(19)        NOT NULL,
    APP_ENS_CLASSIFICATION_ID    NUMBER(19)        NOT NULL,
    APP_SECURITY_ID              NUMBER(19)        NOT NULL,
    OVERALL_GRADE_LEVEL_ID       NUMBER(19),
    IDENTITY_PROVIDER_ID         NUMBER(19),
    ENS_SUBJECT_ID               NUMBER(19),
    PERSONAL_DATA_PROCESSING_ID  NUMBER(19),
    APPROVAL_DATE                TIMESTAMP(6),
    CONFIDENTIALITY_LEVEL_ID     NUMBER(19),
    INTEGRITY_LEVEL_ID           NUMBER(19),
    TRACEABILITY_LEVEL_ID        NUMBER(19),
    AVAILABILITY_LEVEL_ID        NUMBER(19),
    AUTHENTICITY_LEVEL_ID        NUMBER(19),
    CREATED_AT                   TIMESTAMP(6),
    CREATED_BY                   VARCHAR2(64 CHAR),
    UPDATED_AT                   TIMESTAMP(6),
    UPDATED_BY                   VARCHAR2(64 CHAR),
    DELETED_AT                   TIMESTAMP(6),
    DELETED_BY                   VARCHAR2(64 CHAR),
    AUD_ACTION                   VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE                   TIMESTAMP(6)      NOT NULL,
    AUDIT_USER                   VARCHAR2(64 CHAR)
);

CREATE TABLE INV_APP_SECURITY_RISK
(
    ID              NUMBER(19)        NOT NULL,
    APP_SECURITY_ID NUMBER(19)        NOT NULL,
    LEVEL_ID        NUMBER(19),
    DESCRIPTION     CLOB,
    FIELD_ID        NUMBER(19),
    CREATED_AT      TIMESTAMP(6)      NOT NULL,
    CREATED_BY      VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT      TIMESTAMP(6),
    UPDATED_BY      VARCHAR2(64 CHAR),
    DELETED_AT      TIMESTAMP(6),
    DELETED_BY      VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_SECURITY_RISK IS 'Riscos de seguretat identificats per a una aplicacio, penjant de l''ancoratge INV_APP_SECURITY';
COMMENT ON COLUMN INV_APP_SECURITY_RISK.ID IS 'Unique application security risk tracking primary key';
COMMENT ON COLUMN INV_APP_SECURITY_RISK.APP_SECURITY_ID IS 'Foreign key referencing the parent security anchor';
COMMENT ON COLUMN INV_APP_SECURITY_RISK.LEVEL_ID IS 'Optional foreign key referencing the security level lookup entry';
COMMENT ON COLUMN INV_APP_SECURITY_RISK.DESCRIPTION IS 'Free-text description of the identified risk';
COMMENT ON COLUMN INV_APP_SECURITY_RISK.FIELD_ID IS 'Optional foreign key referencing the functional field/scope catalog entry';
COMMENT ON COLUMN INV_APP_SECURITY_RISK.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_SECURITY_RISK.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_SECURITY_RISK.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_SECURITY_RISK.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_SECURITY_RISK.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_SECURITY_RISK.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_SECURITY_RISK_AUD
(
    AUDIT_ID            NUMBER(19)        NOT NULL,
    APP_SECURITY_RISK_ID NUMBER(19)       NOT NULL,
    APP_SECURITY_ID     NUMBER(19)        NOT NULL,
    LEVEL_ID            NUMBER(19),
    DESCRIPTION         CLOB,
    FIELD_ID            NUMBER(19),
    CREATED_AT          TIMESTAMP(6),
    CREATED_BY          VARCHAR2(64 CHAR),
    UPDATED_AT          TIMESTAMP(6),
    UPDATED_BY          VARCHAR2(64 CHAR),
    DELETED_AT          TIMESTAMP(6),
    DELETED_BY          VARCHAR2(64 CHAR),
    AUD_ACTION          VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE          TIMESTAMP(6)      NOT NULL,
    AUDIT_USER          VARCHAR2(64 CHAR)
);

CREATE TABLE INV_APP_SECURITY_MEASURE
(
    ID                 NUMBER(19)        NOT NULL,
    APP_SECURITY_ID    NUMBER(19)        NOT NULL,
    TYPE_ID            NUMBER(19),
    ENS_REQUIREMENT_ID NUMBER(19),
    DESCRIPTION        CLOB,
    CREATED_AT         TIMESTAMP(6)      NOT NULL,
    CREATED_BY         VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(64 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_SECURITY_MEASURE IS 'Mesures de seguretat aplicades a una aplicacio, penjant de l''ancoratge INV_APP_SECURITY';
COMMENT ON COLUMN INV_APP_SECURITY_MEASURE.ID IS 'Unique application security measure tracking primary key';
COMMENT ON COLUMN INV_APP_SECURITY_MEASURE.APP_SECURITY_ID IS 'Foreign key referencing the parent security anchor';
COMMENT ON COLUMN INV_APP_SECURITY_MEASURE.TYPE_ID IS 'Optional foreign key referencing the security measure type catalog entry';
COMMENT ON COLUMN INV_APP_SECURITY_MEASURE.ENS_REQUIREMENT_ID IS 'Optional foreign key referencing the ENS requirement catalog entry';
COMMENT ON COLUMN INV_APP_SECURITY_MEASURE.DESCRIPTION IS 'Free-text description of the applied measure';
COMMENT ON COLUMN INV_APP_SECURITY_MEASURE.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_SECURITY_MEASURE.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_SECURITY_MEASURE.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_SECURITY_MEASURE.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_SECURITY_MEASURE.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_SECURITY_MEASURE.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_SECURITY_MEASURE_AUD
(
    AUDIT_ID               NUMBER(19)        NOT NULL,
    APP_SECURITY_MEASURE_ID NUMBER(19)       NOT NULL,
    APP_SECURITY_ID        NUMBER(19)        NOT NULL,
    TYPE_ID                NUMBER(19),
    ENS_REQUIREMENT_ID     NUMBER(19),
    DESCRIPTION            CLOB,
    CREATED_AT             TIMESTAMP(6),
    CREATED_BY              VARCHAR2(64 CHAR),
    UPDATED_AT              TIMESTAMP(6),
    UPDATED_BY              VARCHAR2(64 CHAR),
    DELETED_AT              TIMESTAMP(6),
    DELETED_BY              VARCHAR2(64 CHAR),
    AUD_ACTION              VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE              TIMESTAMP(6)      NOT NULL,
    AUDIT_USER              VARCHAR2(64 CHAR)
);

CREATE TABLE INV_COMPLIANCE_SITUATION
(
    ID         NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES    VARCHAR2(100 CHAR),
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_COMPLIANCE_SITUATION IS 'Catalog table for compliance situation statuses (situacio de cumpliment) within the Manteniments module';
COMMENT ON COLUMN INV_COMPLIANCE_SITUATION.ID IS 'Unique identifier for the compliance situation status';
COMMENT ON COLUMN INV_COMPLIANCE_SITUATION.NAME IS 'Compliance situation status descriptive label';
COMMENT ON COLUMN INV_COMPLIANCE_SITUATION.NAME_ES IS 'Spanish translation descriptive name for the compliance situation status';
COMMENT ON COLUMN INV_COMPLIANCE_SITUATION.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_COMPLIANCE_SITUATION.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_COMPLIANCE_SITUATION.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_COMPLIANCE_SITUATION.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_COMPLIANCE_SITUATION.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_COMPLIANCE_SITUATION.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_COMPLIANCE_SITUATION_AUD
(
    AUDIT_ID                     NUMBER(19)         NOT NULL,
    COMPLIANCE_SITUATION_ID  NUMBER(19)         NOT NULL,
    NAME                         VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES                      VARCHAR2(100 CHAR),
    CREATED_AT                   TIMESTAMP(6),
    CREATED_BY                   VARCHAR2(64 CHAR),
    UPDATED_AT                   TIMESTAMP(6),
    UPDATED_BY                   VARCHAR2(64 CHAR),
    DELETED_AT                   TIMESTAMP(6),
    DELETED_BY                   VARCHAR2(64 CHAR),
    AUD_ACTION                   VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE                   TIMESTAMP(6)       NOT NULL,
    AUDIT_USER                   VARCHAR2(64 CHAR)
);

CREATE TABLE INV_CLASSIFICATION_SEGMENT
(
    ID         NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES    VARCHAR2(100 CHAR),
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_CLASSIFICATION_SEGMENT IS 'Catalog table for classification segments (segment de classificacio) within the Manteniments module';
COMMENT ON COLUMN INV_CLASSIFICATION_SEGMENT.ID IS 'Unique identifier for the classification segment';
COMMENT ON COLUMN INV_CLASSIFICATION_SEGMENT.NAME IS 'Classification segment descriptive label';
COMMENT ON COLUMN INV_CLASSIFICATION_SEGMENT.NAME_ES IS 'Spanish translation descriptive name for the classification segment';
COMMENT ON COLUMN INV_CLASSIFICATION_SEGMENT.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_CLASSIFICATION_SEGMENT.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_CLASSIFICATION_SEGMENT.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_CLASSIFICATION_SEGMENT.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_CLASSIFICATION_SEGMENT.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_CLASSIFICATION_SEGMENT.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_CLASSIFICATION_SEGMENT_AUD
(
    AUDIT_ID                   NUMBER(19)         NOT NULL,
    CLASSIFICATION_SEGMENT_ID   NUMBER(19)         NOT NULL,
    NAME                       VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES                    VARCHAR2(100 CHAR),
    CREATED_AT                 TIMESTAMP(6),
    CREATED_BY                 VARCHAR2(64 CHAR),
    UPDATED_AT                 TIMESTAMP(6),
    UPDATED_BY                 VARCHAR2(64 CHAR),
    DELETED_AT                 TIMESTAMP(6),
    DELETED_BY                 VARCHAR2(64 CHAR),
    AUD_ACTION                 VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE                 TIMESTAMP(6)       NOT NULL,
    AUDIT_USER                 VARCHAR2(64 CHAR)
);

CREATE TABLE INV_APP_ACCESSIBILITY
(
    ID                          NUMBER(19)         NOT NULL,
    APPLICATION_ID              NUMBER(19)         NOT NULL,
    CLASSIFICATION_SEGMENT_ID    NUMBER(19),
    COMPLIANCE_ID               NUMBER(19),
    PUBLIC_URL                  VARCHAR2(255 CHAR),
    MOBILE_APPLICATION          NUMBER(1),
    MOBILE_APPLICATION_NAME     VARCHAR2(255 CHAR),
    NON_ACCESSIBLE_CONTENT      CLOB,
    OBSERVATIONS                CLOB,
    EXPIRE_DATE                 TIMESTAMP(6),
    CREATED_AT                  TIMESTAMP(6)       NOT NULL,
    CREATED_BY                  VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT                  TIMESTAMP(6),
    UPDATED_BY                  VARCHAR2(64 CHAR),
    DELETED_AT                  TIMESTAMP(6),
    DELETED_BY                  VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_ACCESSIBILITY IS 'Informacio d''accessibilitat (WCAG / UNE-EN 301549) associada a una aplicacio, penjant directament de INV_APPLICATION';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.ID IS 'Unique application accessibility tracking primary key';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.APPLICATION_ID IS 'Foreign key referencing the parent corporate application';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.CLASSIFICATION_SEGMENT_ID IS 'Optional foreign key referencing the classification segment catalog entry';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.COMPLIANCE_ID IS 'Optional foreign key referencing the compliance situation status catalog entry';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.PUBLIC_URL IS 'URL publica del portal web';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.MOBILE_APPLICATION IS 'Indica si la aplicacion dispone de aplicacion movil asociada';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.MOBILE_APPLICATION_NAME IS 'Nombre de la aplicacion movil asociada';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.NON_ACCESSIBLE_CONTENT IS 'Contenido no accesible y justificacion de por que no se ha corregido';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.OBSERVATIONS IS 'Observaciones relevantes en materia de accesibilidad';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.EXPIRE_DATE IS 'Fecha de fin de vigencia de la evaluacion de accesibilidad';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_ACCESSIBILITY.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_ACCESSIBILITY_AUD
(
    AUDIT_ID                    NUMBER(19)         NOT NULL,
    APP_ACCESSIBILITY_ID        NUMBER(19)         NOT NULL,
    APPLICATION_ID               NUMBER(19)         NOT NULL,
    CLASSIFICATION_SEGMENT_ID     NUMBER(19),
    COMPLIANCE_ID                NUMBER(19),
    MOBILE_APPLICATION           NUMBER(1),
    MOBILE_APPLICATION_NAME      VARCHAR2(255 CHAR),
    PUBLIC_URL                   VARCHAR2(255 CHAR),
    NON_ACCESSIBLE_CONTENT       CLOB,
    OBSERVATIONS                 CLOB,
    EXPIRE_DATE                  TIMESTAMP(6),
    CREATED_AT                   TIMESTAMP(6),
    CREATED_BY                   VARCHAR2(64 CHAR),
    UPDATED_AT                   TIMESTAMP(6),
    UPDATED_BY                   VARCHAR2(64 CHAR),
    DELETED_AT                   TIMESTAMP(6),
    DELETED_BY                   VARCHAR2(64 CHAR),
    AUD_ACTION                   VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE                   TIMESTAMP(6)       NOT NULL,
    AUDIT_USER                   VARCHAR2(64 CHAR)
);

ALTER TABLE INV_APPLICATION ADD (ADM_UNIT_CODE VARCHAR2(20 CHAR));
ALTER TABLE INV_APPLICATION_AUD ADD (ADM_UNIT_CODE VARCHAR2(20 CHAR));
