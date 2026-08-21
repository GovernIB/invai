CREATE TABLE INV_COMPANY
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

COMMENT ON TABLE INV_COMPANY IS 'Catalog table for companies within the Manteniments / Responsables module';
COMMENT ON COLUMN INV_COMPANY.ID IS 'Unique identifier for the company';
COMMENT ON COLUMN INV_COMPANY.NAME IS 'Company corporate name';
COMMENT ON COLUMN INV_COMPANY.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_COMPANY.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_COMPANY.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_COMPANY.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_COMPANY.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_COMPANY.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_COMPANY_AUD
(
    AUDIT_ID   NUMBER(19)         NOT NULL,
    COMPANY_ID NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(150 CHAR) NOT NULL,
    CREATED_AT TIMESTAMP(6),
    CREATED_BY VARCHAR2(64 CHAR),
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR),
    AUD_ACTION VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE TIMESTAMP(6)       NOT NULL,
    AUDIT_USER VARCHAR2(64 CHAR)
);

CREATE TABLE INV_PERSON
(
    ID               NUMBER(19)         NOT NULL,
    COMPANY_ID       NUMBER(19),
    FIRST_NAME       VARCHAR2(150 CHAR) NOT NULL,
    LAST_NAME        VARCHAR2(150 CHAR) NOT NULL,
    EMAIL            VARCHAR2(150 CHAR) NOT NULL,
    IS_PERSONAL_CAIB NUMBER(1)          DEFAULT 0 NOT NULL,
    CREATED_AT       TIMESTAMP(6)       NOT NULL,
    CREATED_BY       VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT       TIMESTAMP(6),
    UPDATED_BY       VARCHAR2(64 CHAR),
    DELETED_AT       TIMESTAMP(6),
    DELETED_BY       VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_PERSON IS 'Catalog table for persons within the Manteniments / Responsables module, each linked to a company';
COMMENT ON COLUMN INV_PERSON.ID IS 'Unique identifier for the person';
COMMENT ON COLUMN INV_PERSON.COMPANY_ID IS 'Foreign key referencing the company catalog (INV_COMPANY); null when IS_PERSONAL_CAIB is set';
COMMENT ON COLUMN INV_PERSON.FIRST_NAME IS 'Person first name or identification label';
COMMENT ON COLUMN INV_PERSON.LAST_NAME IS 'Person last name(s)';
COMMENT ON COLUMN INV_PERSON.EMAIL IS 'Contact e-mail address of the person';
COMMENT ON COLUMN INV_PERSON.IS_PERSONAL_CAIB IS 'Whether this person is CAIB staff (sourced via the future DIR3 integration) rather than an external company contact';
COMMENT ON COLUMN INV_PERSON.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_PERSON.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_PERSON.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_PERSON.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_PERSON.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_PERSON.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_PERSON_AUD
(
    AUDIT_ID         NUMBER(19)         NOT NULL,
    PERSON_ID        NUMBER(19)         NOT NULL,
    COMPANY_ID       NUMBER(19),
    FIRST_NAME       VARCHAR2(150 CHAR) NOT NULL,
    LAST_NAME        VARCHAR2(150 CHAR) NOT NULL,
    EMAIL            VARCHAR2(150 CHAR) NOT NULL,
    IS_PERSONAL_CAIB NUMBER(1),
    CREATED_AT       TIMESTAMP(6),
    CREATED_BY       VARCHAR2(64 CHAR),
    UPDATED_AT       TIMESTAMP(6),
    UPDATED_BY       VARCHAR2(64 CHAR),
    DELETED_AT       TIMESTAMP(6),
    DELETED_BY       VARCHAR2(64 CHAR),
    AUD_ACTION       VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE       TIMESTAMP(6)       NOT NULL,
    AUDIT_USER       VARCHAR2(64 CHAR)
);

CREATE TABLE INV_AUTHORIZATION_TYPE
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

COMMENT ON TABLE INV_AUTHORIZATION_TYPE IS 'Catalog table for authorization/capability types within the Manteniments / Responsables module';
COMMENT ON COLUMN INV_AUTHORIZATION_TYPE.ID IS 'Unique identifier for the authorization type';
COMMENT ON COLUMN INV_AUTHORIZATION_TYPE.NAME IS 'Authorization type descriptive label (e.g. "Firmar peticiones")';
COMMENT ON COLUMN INV_AUTHORIZATION_TYPE.NAME_ES IS 'Spanish translation descriptive name for the authorization type';
COMMENT ON COLUMN INV_AUTHORIZATION_TYPE.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_AUTHORIZATION_TYPE.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_AUTHORIZATION_TYPE.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_AUTHORIZATION_TYPE.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_AUTHORIZATION_TYPE.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_AUTHORIZATION_TYPE.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_AUTHORIZATION_TYPE_AUD
(
    AUDIT_ID             NUMBER(19)         NOT NULL,
    AUTHORIZATION_TYPE_ID NUMBER(19)        NOT NULL,
    NAME                 VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES              VARCHAR2(100 CHAR),
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

CREATE TABLE INV_LKUP_RESPONSIBLE_TYPE
(
    ID                     NUMBER(19)         NOT NULL,
    NAME                   VARCHAR2(150 CHAR) NOT NULL,
    NAME_ES                VARCHAR2(100 CHAR) NOT NULL,
    REQUIRES_PERSONAL_CAIB NUMBER(1) DEFAULT 0 NOT NULL
);

COMMENT ON TABLE INV_LKUP_RESPONSIBLE_TYPE IS 'Lookup table for responsible types assignable to an application within the Responsables module';
COMMENT ON COLUMN INV_LKUP_RESPONSIBLE_TYPE.ID IS 'Unique identifier for the responsible type';
COMMENT ON COLUMN INV_LKUP_RESPONSIBLE_TYPE.NAME IS 'Responsible type descriptive label (e.g. "Responsable funcional")';
COMMENT ON COLUMN INV_LKUP_RESPONSIBLE_TYPE.NAME_ES IS 'Spanish translation descriptive name for the responsible type';
COMMENT ON COLUMN INV_LKUP_RESPONSIBLE_TYPE.REQUIRES_PERSONAL_CAIB IS 'Whether this responsible type may only be held by Personal CAIB persons';

CREATE TABLE INV_APP_RESPONSIBLE_AUTHORIZED
(
    ID             NUMBER(19)   NOT NULL,
    APPLICATION_ID NUMBER(19)   NOT NULL,
    CREATED_AT     TIMESTAMP(6) NOT NULL,
    CREATED_BY     VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT     TIMESTAMP(6),
    UPDATED_BY     VARCHAR2(64 CHAR),
    DELETED_AT     TIMESTAMP(6),
    DELETED_BY     VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_RESPONSIBLE_AUTHORIZED IS 'Anchor row binding the "Responsables i Autoritzats" tab to a corporate application, mirroring INV_APP_INFORMATION_SYSTEM_DB/INV_APP_DEVELOPMENT: one row per application, referenced by INV_APP_RESPONSIBLE and INV_APP_AUTHORIZED instead of the application directly';
COMMENT ON COLUMN INV_APP_RESPONSIBLE_AUTHORIZED.ID IS 'Unique identifier for the anchor';
COMMENT ON COLUMN INV_APP_RESPONSIBLE_AUTHORIZED.APPLICATION_ID IS 'Foreign key referencing the owning application (INV_APPLICATION)';
COMMENT ON COLUMN INV_APP_RESPONSIBLE_AUTHORIZED.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_RESPONSIBLE_AUTHORIZED.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_RESPONSIBLE_AUTHORIZED.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_RESPONSIBLE_AUTHORIZED.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_RESPONSIBLE_AUTHORIZED.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_RESPONSIBLE_AUTHORIZED.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_RESPONSIBLE_AUTHORIZED_AUD
(
    AUDIT_ID          NUMBER(19)   NOT NULL,
    APP_RESPONSIBLE_AUTHORIZED_ID NUMBER(19)  NOT NULL,
    APPLICATION_ID    NUMBER(19)   NOT NULL,
    CREATED_AT        TIMESTAMP(6),
    CREATED_BY        VARCHAR2(64 CHAR),
    UPDATED_AT        TIMESTAMP(6),
    UPDATED_BY        VARCHAR2(64 CHAR),
    DELETED_AT        TIMESTAMP(6),
    DELETED_BY        VARCHAR2(64 CHAR),
    AUD_ACTION        VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE        TIMESTAMP(6) NOT NULL,
    AUDIT_USER        VARCHAR2(64 CHAR)
);

CREATE TABLE INV_APP_RESPONSIBLE
(
    ID                  NUMBER(19)   NOT NULL,
    APP_RESPONSIBLE_AUTHORIZED_ID  NUMBER(19)   NOT NULL,
    PERSON_ID           NUMBER(19)   NOT NULL,
    RESPONSIBLE_TYPE_ID NUMBER(19)   NOT NULL,
    JOB_TITLE           VARCHAR2(150 CHAR),
    OBSERVATION         CLOB,
    CREATED_AT          TIMESTAMP(6) NOT NULL,
    CREATED_BY          VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT          TIMESTAMP(6),
    UPDATED_BY          VARCHAR2(64 CHAR),
    DELETED_AT          TIMESTAMP(6),
    DELETED_BY          VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_RESPONSIBLE IS 'Assigns a person as the holder of a responsible type for an application''s Responsables tab (via INV_APP_RESPONSIBLE_AUTHORIZED); at most one active person per (appResponsibleAuthorized, responsible type), enforced at the service layer';
COMMENT ON COLUMN INV_APP_RESPONSIBLE.ID IS 'Unique identifier for the application responsible assignment';
COMMENT ON COLUMN INV_APP_RESPONSIBLE.APP_RESPONSIBLE_AUTHORIZED_ID IS 'Foreign key referencing the parent Responsables tab anchor (INV_APP_RESPONSIBLE_AUTHORIZED)';
COMMENT ON COLUMN INV_APP_RESPONSIBLE.PERSON_ID IS 'Foreign key referencing the assigned person (INV_PERSON)';
COMMENT ON COLUMN INV_APP_RESPONSIBLE.RESPONSIBLE_TYPE_ID IS 'Foreign key referencing the responsible type held (INV_LKUP_RESPONSIBLE_TYPE)';
COMMENT ON COLUMN INV_APP_RESPONSIBLE.JOB_TITLE IS 'Free-text job title/position held by the responsible person for this assignment';
COMMENT ON COLUMN INV_APP_RESPONSIBLE.OBSERVATION IS 'Free-text reason captured at deactivation ("Donar de baixa")';
COMMENT ON COLUMN INV_APP_RESPONSIBLE.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_RESPONSIBLE.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_RESPONSIBLE.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_RESPONSIBLE.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_RESPONSIBLE.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_RESPONSIBLE.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_RESPONSIBLE_AUD
(
    AUDIT_ID                   NUMBER(19)  NOT NULL,
    APP_RESPONSIBLE_ID NUMBER(19)  NOT NULL,
    APP_RESPONSIBLE_AUTHORIZED_ID         NUMBER(19)  NOT NULL,
    PERSON_ID                 NUMBER(19)   NOT NULL,
    RESPONSIBLE_TYPE_ID       NUMBER(19)   NOT NULL,
    JOB_TITLE                 VARCHAR2(150 CHAR),
    OBSERVATION               CLOB,
    CREATED_AT                TIMESTAMP(6),
    CREATED_BY                VARCHAR2(64 CHAR),
    UPDATED_AT                TIMESTAMP(6),
    UPDATED_BY                VARCHAR2(64 CHAR),
    DELETED_AT                TIMESTAMP(6),
    DELETED_BY                VARCHAR2(64 CHAR),
    AUD_ACTION                VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE                TIMESTAMP(6) NOT NULL,
    AUDIT_USER                VARCHAR2(64 CHAR)
);

CREATE TABLE INV_APP_AUTHORIZED
(
    ID                 NUMBER(19)   NOT NULL,
    APP_RESPONSIBLE_AUTHORIZED_ID NUMBER(19)   NOT NULL,
    PERSON_ID          NUMBER(19)   NOT NULL,
    OBSERVATION        CLOB,
    CREATED_AT         TIMESTAMP(6) NOT NULL,
    CREATED_BY         VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(64 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_AUTHORIZED IS 'Anchor row assigning a person as authorized on an application''s Responsables tab (via INV_APP_RESPONSIBLE_AUTHORIZED); at most one active anchor per (appResponsibleAuthorized, person), enforced at the service layer. Authorization types held are tracked via INV_APP_AUTHORIZED_TYPE_LINK';
COMMENT ON COLUMN INV_APP_AUTHORIZED.ID IS 'Unique identifier for the application authorized assignment';
COMMENT ON COLUMN INV_APP_AUTHORIZED.APP_RESPONSIBLE_AUTHORIZED_ID IS 'Foreign key referencing the parent Responsables tab anchor (INV_APP_RESPONSIBLE_AUTHORIZED)';
COMMENT ON COLUMN INV_APP_AUTHORIZED.PERSON_ID IS 'Foreign key referencing the authorized person (INV_PERSON)';
COMMENT ON COLUMN INV_APP_AUTHORIZED.OBSERVATION IS 'Free-text reason captured at deactivation ("Donar de baixa")';
COMMENT ON COLUMN INV_APP_AUTHORIZED.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_AUTHORIZED.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_AUTHORIZED.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_AUTHORIZED.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_AUTHORIZED.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_AUTHORIZED.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_AUTHORIZED_AUD
(
    AUDIT_ID                  NUMBER(19)   NOT NULL,
    APP_AUTHORIZED_ID NUMBER(19)   NOT NULL,
    APP_RESPONSIBLE_AUTHORIZED_ID        NUMBER(19)   NOT NULL,
    PERSON_ID                 NUMBER(19)   NOT NULL,
    OBSERVATION                CLOB,
    CREATED_AT               TIMESTAMP(6),
    CREATED_BY               VARCHAR2(64 CHAR),
    UPDATED_AT               TIMESTAMP(6),
    UPDATED_BY               VARCHAR2(64 CHAR),
    DELETED_AT               TIMESTAMP(6),
    DELETED_BY               VARCHAR2(64 CHAR),
    AUD_ACTION               VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE               TIMESTAMP(6) NOT NULL,
    AUDIT_USER                VARCHAR2(64 CHAR)
);

CREATE TABLE INV_APP_AUTHORIZED_TYPE_LINK
(
    ID                        NUMBER(19) NOT NULL,
    APP_AUTHORIZED_ID NUMBER(19) NOT NULL,
    AUTHORIZATION_TYPE_ID     NUMBER(19) NOT NULL
);

COMMENT ON TABLE INV_APP_AUTHORIZED_TYPE_LINK IS 'Intermediate join table mapping an application authorized assignment (INV_APP_AUTHORIZED) to each authorization type it holds (INV_AUTHORIZATION_TYPE); one row per attached type, hard-deleted on detach, no audit trail';
COMMENT ON COLUMN INV_APP_AUTHORIZED_TYPE_LINK.ID IS 'Unique identifier for the application authorized type link';
COMMENT ON COLUMN INV_APP_AUTHORIZED_TYPE_LINK.APP_AUTHORIZED_ID IS 'Foreign key referencing the parent application authorized assignment (INV_APP_AUTHORIZED)';
COMMENT ON COLUMN INV_APP_AUTHORIZED_TYPE_LINK.AUTHORIZATION_TYPE_ID IS 'Foreign key referencing the authorization type held (INV_AUTHORIZATION_TYPE)';
