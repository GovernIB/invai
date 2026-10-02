CREATE TABLE INV_LKUP_DIR3_STATUS
(
    ID      NUMBER(19)        NOT NULL,
    CODE    VARCHAR2(20 CHAR) NOT NULL,
    NAME    VARCHAR2(64 CHAR) NOT NULL,
    NAME_ES VARCHAR2(64 CHAR) NOT NULL
);

COMMENT ON TABLE INV_LKUP_DIR3_STATUS IS 'Lookup table for the fixed DIR3 validation states (VALIDATED/NOT_VALIDATED/MANUAL/NA), Catalan/Spanish names';
COMMENT ON COLUMN INV_LKUP_DIR3_STATUS.ID IS 'Unique surrogate key for the DIR3 status entry';
COMMENT ON COLUMN INV_LKUP_DIR3_STATUS.CODE IS 'Fixed technical code for the state (e.g. VALIDATED)';
COMMENT ON COLUMN INV_LKUP_DIR3_STATUS.NAME IS 'Display label of the state, Catalan';
COMMENT ON COLUMN INV_LKUP_DIR3_STATUS.NAME_ES IS 'Display label of the state, Spanish';

CREATE TABLE INV_DIR3_VALIDATION
(
    ID                  NUMBER(19)        NOT NULL,
    DIR3_STATUS_ID      NUMBER(19)        NOT NULL,
    MANUAL_VALIDATED_AT TIMESTAMP(6),
    MANUAL_VALIDATED_BY VARCHAR2(64 CHAR),
    REASON              VARCHAR2(4000 CHAR),
    CREATED_AT          TIMESTAMP(6)      NOT NULL,
    CREATED_BY          VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT          TIMESTAMP(6),
    UPDATED_BY          VARCHAR2(64 CHAR),
    DELETED_AT          TIMESTAMP(6),
    DELETED_BY          VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_DIR3_VALIDATION IS 'DIR3 validation state of a single AppResponsible/AppAuthorized assignment, kept as its own entity so it has a dedicated audit trail (INV_DIR3_VALIDATION_AUD) independent of unrelated field changes on the owning assignment';
COMMENT ON COLUMN INV_DIR3_VALIDATION.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_DIR3_VALIDATION.DIR3_STATUS_ID IS 'Current DIR3 validation state (INV_LKUP_DIR3_STATUS)';
COMMENT ON COLUMN INV_DIR3_VALIDATION.MANUAL_VALIDATED_AT IS 'Timestamp of the manual DIR3 validation, populated only when DIR3_STATUS_ID = MANUAL';
COMMENT ON COLUMN INV_DIR3_VALIDATION.MANUAL_VALIDATED_BY IS 'User who performed the manual DIR3 validation';
COMMENT ON COLUMN INV_DIR3_VALIDATION.REASON IS 'Free-text justification given for the manual DIR3 validation, populated only when DIR3_STATUS_ID = MANUAL';
COMMENT ON COLUMN INV_DIR3_VALIDATION.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_DIR3_VALIDATION.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_DIR3_VALIDATION.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_DIR3_VALIDATION.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_DIR3_VALIDATION.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_DIR3_VALIDATION.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_DIR3_VALIDATION_AUD
(
    AUDIT_ID            NUMBER(19)        NOT NULL,
    DIR3_VALIDATION_ID  NUMBER(19)        NOT NULL,
    DIR3_STATUS_ID      NUMBER(19)        NOT NULL,
    MANUAL_VALIDATED_AT TIMESTAMP(6),
    MANUAL_VALIDATED_BY VARCHAR2(64 CHAR),
    REASON              VARCHAR2(4000 CHAR),
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

COMMENT ON TABLE INV_DIR3_VALIDATION_AUD IS 'Historical audit trail mirroring every INV_DIR3_VALIDATION mutation';

CREATE TABLE INV_APP_DATA
(
    ID                 NUMBER(19)        NOT NULL,
    APPLICATION_ID     NUMBER(19)        NOT NULL,
    OBSERVATION        VARCHAR2(4000 CHAR),
    OPEN_DATA_URL      VARCHAR2(500 CHAR),
    USE_OPEN_DATA_URL  NUMBER(1) DEFAULT 0 NOT NULL,
    REUSE_URL          VARCHAR2(500 CHAR),
    USE_REUSE_URL      NUMBER(1) DEFAULT 0 NOT NULL,
    CREATED_AT         TIMESTAMP(6)      NOT NULL,
    CREATED_BY         VARCHAR2(50 CHAR) NOT NULL,
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(50 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_DATA IS 'Anchor for the "data" tabs (Open Data + Reutilitzacio) of an application; holds no swagger/OpenAPI data itself, only the free-text observation and the optional explicit query URLs - the published GET endpoints are resolved live on every read, either from the application''s own external REST API/backend or, when the matching USE_*_URL flag is set, from the matching *_URL column instead';
COMMENT ON COLUMN INV_APP_DATA.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_APP_DATA.APPLICATION_ID IS 'Owning application (INV_APPLICATION)';
COMMENT ON COLUMN INV_APP_DATA.OBSERVATION IS 'Free-text observations about this application''s open data / external REST API';
COMMENT ON COLUMN INV_APP_DATA.OPEN_DATA_URL IS 'Explicit OpenAPI document URL to query instead of the application-code-derived one, used only when USE_OPEN_DATA_URL is set';
COMMENT ON COLUMN INV_APP_DATA.USE_OPEN_DATA_URL IS 'Whether to query OPEN_DATA_URL instead of deriving the URL from the application''s own code; defaults to false';
COMMENT ON COLUMN INV_APP_DATA.REUSE_URL IS 'Explicit reuse document URL to query instead of the application-code-derived one, used only when USE_REUSE_URL is set';
COMMENT ON COLUMN INV_APP_DATA.USE_REUSE_URL IS 'Whether to query REUSE_URL instead of deriving the URL from the application''s own code; defaults to false';
COMMENT ON COLUMN INV_APP_DATA.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_DATA.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_DATA.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_DATA.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_DATA.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_DATA.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_DATA_AUD
(
    AUDIT_ID           NUMBER(19)        NOT NULL,
    APP_DATA_ID        NUMBER(19)        NOT NULL,
    APPLICATION_ID     NUMBER(19)        NOT NULL,
    OBSERVATION        VARCHAR2(4000 CHAR),
    OPEN_DATA_URL      VARCHAR2(500 CHAR),
    USE_OPEN_DATA_URL  NUMBER(1),
    REUSE_URL          VARCHAR2(500 CHAR),
    USE_REUSE_URL      NUMBER(1),
    CREATED_AT         TIMESTAMP(6),
    CREATED_BY         VARCHAR2(50 CHAR),
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(50 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR),
    AUD_ACTION         VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE         TIMESTAMP(6)      NOT NULL,
    AUDIT_USER         VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_DATA_AUD IS 'Historical audit trail mirroring every INV_APP_DATA mutation';

CREATE TABLE INV_EXTERNAL_SYSTEM
(
    ID         NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(150 CHAR) NOT NULL,
    COMPANY_ID NUMBER(19)         NOT NULL,
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_EXTERNAL_SYSTEM IS 'Catalog of external systems (outside the inventory) that an application can integrate with, for the "Integracio" tab''s "Sistema" column';
COMMENT ON COLUMN INV_EXTERNAL_SYSTEM.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_EXTERNAL_SYSTEM.NAME IS 'External system name';
COMMENT ON COLUMN INV_EXTERNAL_SYSTEM.COMPANY_ID IS 'Company responsible for the external system (INV_COMPANY)';
COMMENT ON COLUMN INV_EXTERNAL_SYSTEM.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_EXTERNAL_SYSTEM.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_EXTERNAL_SYSTEM.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_EXTERNAL_SYSTEM.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_EXTERNAL_SYSTEM.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_EXTERNAL_SYSTEM.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_EXTERNAL_SYSTEM_AUD
(
    AUDIT_ID           NUMBER(19)         NOT NULL,
    EXTERNAL_SYSTEM_ID NUMBER(19)         NOT NULL,
    NAME               VARCHAR2(150 CHAR) NOT NULL,
    COMPANY_ID         NUMBER(19)         NOT NULL,
    CREATED_AT         TIMESTAMP(6),
    CREATED_BY         VARCHAR2(64 CHAR),
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(64 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR),
    AUD_ACTION         VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE         TIMESTAMP(6)       NOT NULL,
    AUDIT_USER         VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_EXTERNAL_SYSTEM_AUD IS 'Historical audit trail mirroring every INV_EXTERNAL_SYSTEM mutation';

CREATE TABLE INV_APP_INTEGRATION
(
    ID             NUMBER(19)   NOT NULL,
    APPLICATION_ID NUMBER(19)   NOT NULL,
    OBSERVATION    VARCHAR2(4000 CHAR),
    CREATED_AT     TIMESTAMP(6) NOT NULL,
    CREATED_BY     VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT     TIMESTAMP(6),
    UPDATED_BY     VARCHAR2(64 CHAR),
    DELETED_AT     TIMESTAMP(6),
    DELETED_BY     VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_INTEGRATION IS 'Anchor for the "Integracio" tab of an application; holds only the free-text observation - its rows (INV_APP_INTEGRATION_CONN) carry the actual system/technology/user/role data';
COMMENT ON COLUMN INV_APP_INTEGRATION.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_APP_INTEGRATION.APPLICATION_ID IS 'Owning application (INV_APPLICATION)';
COMMENT ON COLUMN INV_APP_INTEGRATION.OBSERVATION IS 'Free-text observations about this application''s integrations';
COMMENT ON COLUMN INV_APP_INTEGRATION.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_INTEGRATION.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_INTEGRATION.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_INTEGRATION.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_INTEGRATION.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_INTEGRATION.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_INTEGRATION_AUD
(
    AUDIT_ID           NUMBER(19)   NOT NULL,
    APP_INTEGRATION_ID NUMBER(19)   NOT NULL,
    APPLICATION_ID     NUMBER(19)   NOT NULL,
    OBSERVATION        VARCHAR2(4000 CHAR),
    CREATED_AT         TIMESTAMP(6),
    CREATED_BY         VARCHAR2(64 CHAR),
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(64 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR),
    AUD_ACTION         VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE         TIMESTAMP(6)      NOT NULL,
    AUDIT_USER         VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_INTEGRATION_AUD IS 'Historical audit trail mirroring every INV_APP_INTEGRATION mutation';

CREATE TABLE INV_APP_INTEGRATION_CONN
(
    ID                 NUMBER(19)         NOT NULL,
    APP_INTEGRATION_ID NUMBER(19)         NOT NULL,
    APPLICATION_ID     NUMBER(19),
    EXTERNAL_SYSTEM_ID NUMBER(19),
    TECHNOLOGY_ID      NUMBER(19)         NOT NULL,
    USERNAME           VARCHAR2(100 CHAR) NOT NULL,
    CREATED_AT         TIMESTAMP(6)       NOT NULL,
    CREATED_BY         VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(64 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_INTEGRATION_CONN IS 'One row per system this application integrates with; exactly one of APPLICATION_ID/EXTERNAL_SYSTEM_ID must be set (validated in the facade, not enforced by a DB constraint)';
COMMENT ON COLUMN INV_APP_INTEGRATION_CONN.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_APP_INTEGRATION_CONN.APP_INTEGRATION_ID IS 'Owning integration anchor (INV_APP_INTEGRATION)';
COMMENT ON COLUMN INV_APP_INTEGRATION_CONN.APPLICATION_ID IS 'The other system, when it is an application already registered in the inventory (INV_APPLICATION); null when EXTERNAL_SYSTEM_ID is set instead';
COMMENT ON COLUMN INV_APP_INTEGRATION_CONN.EXTERNAL_SYSTEM_ID IS 'The other system, when it is outside the inventory (INV_EXTERNAL_SYSTEM); null when APPLICATION_ID is set instead';
COMMENT ON COLUMN INV_APP_INTEGRATION_CONN.TECHNOLOGY_ID IS 'Technology used by this integration (INV_TECHNOLOGY)';
COMMENT ON COLUMN INV_APP_INTEGRATION_CONN.USERNAME IS 'Snapshot of the Soffid username at the time it was selected';
COMMENT ON COLUMN INV_APP_INTEGRATION_CONN.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_INTEGRATION_CONN.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_INTEGRATION_CONN.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_INTEGRATION_CONN.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_INTEGRATION_CONN.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_INTEGRATION_CONN.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_INTEGRATION_CONN_AUD
(
    AUDIT_ID                 NUMBER(19)         NOT NULL,
    APP_INTEGRATION_CONN_ID NUMBER(19)         NOT NULL,
    APP_INTEGRATION_ID       NUMBER(19)         NOT NULL,
    APPLICATION_ID           NUMBER(19),
    EXTERNAL_SYSTEM_ID       NUMBER(19),
    TECHNOLOGY_ID            NUMBER(19)         NOT NULL,
    USERNAME                 VARCHAR2(100 CHAR) NOT NULL,
    CREATED_AT               TIMESTAMP(6),
    CREATED_BY               VARCHAR2(64 CHAR),
    UPDATED_AT               TIMESTAMP(6),
    UPDATED_BY               VARCHAR2(64 CHAR),
    DELETED_AT               TIMESTAMP(6),
    DELETED_BY               VARCHAR2(64 CHAR),
    AUD_ACTION               VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE               TIMESTAMP(6)       NOT NULL,
    AUDIT_USER               VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_INTEGRATION_CONN_AUD IS 'Historical audit trail mirroring every INV_APP_INTEGRATION_CONN mutation';

CREATE TABLE INV_APP_INTEGR_REQ_RL
(
    ID                       NUMBER(19)        NOT NULL,
    APP_INTEGRATION_CONN_ID NUMBER(19)        NOT NULL,
    ROLE_ID                  NUMBER(19)        NOT NULL,
    CREATED_AT               TIMESTAMP(6)      NOT NULL,
    CREATED_BY               VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT               TIMESTAMP(6),
    UPDATED_BY               VARCHAR2(64 CHAR),
    DELETED_AT               TIMESTAMP(6),
    DELETED_BY               VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_INTEGR_REQ_RL IS 'Roles required by an integration connection; only the Soffid role id is kept (no FK to any local catalog) - name/system/description are resolved live from Soffid on read, never persisted';
COMMENT ON COLUMN INV_APP_INTEGR_REQ_RL.ID IS 'Unique identifier';
COMMENT ON COLUMN INV_APP_INTEGR_REQ_RL.APP_INTEGRATION_CONN_ID IS 'Owning integration connection (INV_APP_INTEGRATION_CONN)';
COMMENT ON COLUMN INV_APP_INTEGR_REQ_RL.ROLE_ID IS 'Soffid role''s own numeric id';
COMMENT ON COLUMN INV_APP_INTEGR_REQ_RL.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_INTEGR_REQ_RL.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_APP_INTEGR_REQ_RL.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_INTEGR_REQ_RL.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_APP_INTEGR_REQ_RL.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_INTEGR_REQ_RL.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_INTEGR_REQ_RL_AUD
(
    AUDIT_ID                     NUMBER(19)        NOT NULL,
    APP_INTEGRATION_REQ_RL_ID  NUMBER(19)        NOT NULL,
    APP_INTEGRATION_CONN_ID     NUMBER(19)        NOT NULL,
    ROLE_ID                      NUMBER(19)        NOT NULL,
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

COMMENT ON TABLE INV_APP_INTEGR_REQ_RL_AUD IS 'Historical audit trail mirroring every INV_APP_INTEGR_REQ_RL mutation';

CREATE TABLE INV_APP_AUTHORIZED_TYPE_LINK_AUD
(
    AUDIT_ID                     NUMBER(19)        NOT NULL,
    APP_AUTHORIZED_TYPE_LINK_ID  NUMBER(19)        NOT NULL,
    APP_AUTHORIZED_ID            NUMBER(19)        NOT NULL,
    AUTHORIZATION_TYPE_ID        NUMBER(19)        NOT NULL,
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

COMMENT ON TABLE INV_APP_AUTHORIZED_TYPE_LINK_AUD IS 'Historical audit trail mirroring every INV_APP_AUTHORIZED_TYPE_LINK mutation - added in 1.0.5, retrofitting audit onto a join table that had none since its own 1.0.3 introduction';
