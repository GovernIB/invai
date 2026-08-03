CREATE TABLE INV_ENVIRONMENT
(
    ID         NUMBER(19)         NOT NULL,
    CODE       VARCHAR2(50 CHAR)  NOT NULL,
    NAME       VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES    VARCHAR2(100 CHAR) NOT NULL,
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_ENVIRONMENT IS 'Environment mapping technical environment codes to Catalan and Spanish names';
COMMENT ON COLUMN INV_ENVIRONMENT.ID IS 'Unique surrogate key for the environment translation entry';
COMMENT ON COLUMN INV_ENVIRONMENT.CODE IS 'Standardized technical code for the environment (e.g., PRODUCTION)';
COMMENT ON COLUMN INV_ENVIRONMENT.NAME IS 'Catalan translation descriptive name for the environment';
COMMENT ON COLUMN INV_ENVIRONMENT.NAME_ES IS 'Spanish translation descriptive name for the environment';
COMMENT ON COLUMN INV_ENVIRONMENT.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_ENVIRONMENT.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_ENVIRONMENT.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_ENVIRONMENT.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_ENVIRONMENT.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_ENVIRONMENT.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_ENVIRONMENT_AUD
(
    AUDIT_ID       NUMBER(19)         NOT NULL,
    ENVIRONMENT_ID NUMBER(19)         NOT NULL,
    CODE           VARCHAR2(50 CHAR)  NOT NULL,
    NAME           VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES        VARCHAR2(100 CHAR) NOT NULL,
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

CREATE TABLE INV_LKUP_SERVER_TYPE
(
    ID      NUMBER(19)         NOT NULL,
    CODE    VARCHAR2(50 CHAR)  NOT NULL,
    NAME    VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES VARCHAR2(100 CHAR) NOT NULL
);

COMMENT ON TABLE INV_LKUP_SERVER_TYPE IS 'Lookup table for server types';
COMMENT ON COLUMN INV_LKUP_SERVER_TYPE.ID IS 'Unique surrogate key for server type lookup entry';
COMMENT ON COLUMN INV_LKUP_SERVER_TYPE.CODE IS 'Stable technical discriminator code for the server type (e.g., DATABASE, APPLICATION)';
COMMENT ON COLUMN INV_LKUP_SERVER_TYPE.NAME IS 'Catalan translation descriptive name for server type';
COMMENT ON COLUMN INV_LKUP_SERVER_TYPE.NAME_ES IS 'Spanish translation descriptive name for server type';

CREATE TABLE INV_SERVER
(
    ID             NUMBER(19)         NOT NULL,
    NAME           VARCHAR2(255 CHAR) NOT NULL,
    ENVIRONMENT_ID NUMBER(19)         NOT NULL,
    SERVER_TYPE_ID NUMBER(19)         NOT NULL,
    CREATED_AT     TIMESTAMP(6)       NOT NULL,
    CREATED_BY     VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT     TIMESTAMP(6),
    UPDATED_BY     VARCHAR2(64 CHAR),
    DELETED_AT     TIMESTAMP(6),
    DELETED_BY     VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_SERVER IS 'Server inventory catalog containing physical or virtual host machines';
COMMENT ON COLUMN INV_SERVER.ID IS 'Unique tracking identifier for the server';
COMMENT ON COLUMN INV_SERVER.NAME IS 'Host name or server identification label';
COMMENT ON COLUMN INV_SERVER.ENVIRONMENT_ID IS 'Foreign key referencing the deployment environment zone';
COMMENT ON COLUMN INV_SERVER.SERVER_TYPE_ID IS 'Foreign key referencing the server type lookup';
COMMENT ON COLUMN INV_SERVER.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_SERVER.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_SERVER.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_SERVER.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_SERVER.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_SERVER.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_SERVER_AUD
(
    AUDIT_ID       NUMBER(19)         NOT NULL,
    SERVER_ID      NUMBER(19)         NOT NULL,
    NAME           VARCHAR2(255 CHAR) NOT NULL,
    ENVIRONMENT_ID NUMBER(19)         NOT NULL,
    SERVER_TYPE_ID NUMBER(19)         NOT NULL,
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

CREATE TABLE INV_SYSTEM
(
    ID          NUMBER(19)         NOT NULL,
    SERVER_ID   NUMBER(19)         NOT NULL,
    INSTANCE    VARCHAR2(255 CHAR) NOT NULL,
    PORT        NUMBER(9),
    VERSION     VARCHAR2(255 CHAR),
    DESCRIPTION VARCHAR2(255 CHAR),
    CREATED_AT  TIMESTAMP(6)       NOT NULL,
    CREATED_BY  VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT  TIMESTAMP(6),
    UPDATED_BY  VARCHAR2(64 CHAR),
    DELETED_AT  TIMESTAMP(6),
    DELETED_BY  VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_SYSTEM IS 'Server system instances catalog containing physical or virtual infrastructure endpoints';
COMMENT ON COLUMN INV_SYSTEM.ID IS 'Unique tracking identifier for the hardware or software server system instance';
COMMENT ON COLUMN INV_SYSTEM.SERVER_ID IS 'Foreign key referencing the host server (INV_SERVER) this system runs on';
COMMENT ON COLUMN INV_SYSTEM.INSTANCE IS 'Application server instance deploy execution space target description';
COMMENT ON COLUMN INV_SYSTEM.PORT IS 'Access port configured for the system connection';
COMMENT ON COLUMN INV_SYSTEM.VERSION IS 'Technical runtime or framework release version of the host';
COMMENT ON COLUMN INV_SYSTEM.DESCRIPTION IS 'Detailed functional description of the system environment';
COMMENT ON COLUMN INV_SYSTEM.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_SYSTEM.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_SYSTEM.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_SYSTEM.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_SYSTEM.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_SYSTEM.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_SYSTEM_AUD
(
    AUDIT_ID    NUMBER(19)         NOT NULL,
    SYSTEM_ID   NUMBER(19)         NOT NULL,
    SERVER_ID   NUMBER(19)         NOT NULL,
    INSTANCE    VARCHAR2(255 CHAR) NOT NULL,
    PORT        NUMBER(9),
    VERSION     VARCHAR2(255 CHAR),
    DESCRIPTION VARCHAR2(255 CHAR),
    CREATED_AT  TIMESTAMP(6),
    CREATED_BY  VARCHAR2(64 CHAR),
    UPDATED_AT  TIMESTAMP(6),
    UPDATED_BY  VARCHAR2(64 CHAR),
    DELETED_AT  TIMESTAMP(6),
    DELETED_BY  VARCHAR2(64 CHAR),
    AUD_ACTION  VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE  TIMESTAMP(6)       NOT NULL,
    AUDIT_USER  VARCHAR2(64 CHAR)
);

CREATE TABLE INV_DATABASE_VENDOR
(
    ID           NUMBER(19)         NOT NULL,
    NAME         VARCHAR2(100 CHAR) NOT NULL,
    DEFAULT_PORT NUMBER(9),
    CREATED_AT   TIMESTAMP(6)       NOT NULL,
    CREATED_BY   VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT   TIMESTAMP(6),
    UPDATED_BY   VARCHAR2(64 CHAR),
    DELETED_AT   TIMESTAMP(6),
    DELETED_BY   VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_DATABASE_VENDOR IS 'Catalog table for database types and vendors';
COMMENT ON COLUMN INV_DATABASE_VENDOR.ID IS 'Unique identifier for database vendor';
COMMENT ON COLUMN INV_DATABASE_VENDOR.NAME IS 'Name of the database vendor/type (e.g., Oracle, PostgreSQL)';
COMMENT ON COLUMN INV_DATABASE_VENDOR.DEFAULT_PORT IS 'Default connection port for the database vendor';
COMMENT ON COLUMN INV_DATABASE_VENDOR.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_DATABASE_VENDOR.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_DATABASE_VENDOR.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_DATABASE_VENDOR.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_DATABASE_VENDOR.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_DATABASE_VENDOR.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_DATABASE_VENDOR_AUD
(
    AUDIT_ID           NUMBER(19)         NOT NULL,
    DATABASE_VENDOR_ID NUMBER(19)         NOT NULL,
    NAME               VARCHAR2(100 CHAR) NOT NULL,
    DEFAULT_PORT       NUMBER(9),
    CREATED_AT         TIMESTAMP(6)       NOT NULL,
    CREATED_BY         VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(64 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR),
    AUD_ACTION         VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE         TIMESTAMP(6)       NOT NULL,
    AUDIT_USER         VARCHAR2(64 CHAR)
);

CREATE TABLE INV_DATABASE
(
    ID            NUMBER(19)         NOT NULL,
    SERVER_ID     NUMBER(19)         NOT NULL,
    SERVICE       VARCHAR2(255 CHAR) NOT NULL,
    PORT          NUMBER(9),
    DATABASE_TYPE NUMBER(19),
    DESCRIPTION   VARCHAR2(255 CHAR),
    CREATED_AT    TIMESTAMP(6)       NOT NULL,
    CREATED_BY    VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT    TIMESTAMP(6),
    UPDATED_BY    VARCHAR2(64 CHAR),
    DELETED_AT    TIMESTAMP(6),
    DELETED_BY    VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_DATABASE IS 'Database service engine inventory mapping storage endpoints and services';
COMMENT ON COLUMN INV_DATABASE.ID IS 'Unique tracking identifier for the corporate database service engine';
COMMENT ON COLUMN INV_DATABASE.SERVER_ID IS 'Foreign key referencing the hosting server (INV_SERVER); must reference a server whose type is DATABASE';
COMMENT ON COLUMN INV_DATABASE.SERVICE IS 'Database engine service identifier, SID, or database name';
COMMENT ON COLUMN INV_DATABASE.PORT IS 'Access network port configured for database engine listener';
COMMENT ON COLUMN INV_DATABASE.DATABASE_TYPE IS 'Foreign key referencing the database vendor/type catalog (INV_DATABASE_VENDOR)';
COMMENT ON COLUMN INV_DATABASE.DESCRIPTION IS 'Detailed functional description of the database role';
COMMENT ON COLUMN INV_DATABASE.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_DATABASE.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_DATABASE.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_DATABASE.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_DATABASE.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_DATABASE.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_DATABASE_AUD
(
    AUDIT_ID      NUMBER(19)         NOT NULL,
    DATABASE_ID   NUMBER(19)         NOT NULL,
    SERVER_ID     NUMBER(19)         NOT NULL,
    SERVICE       VARCHAR2(255 CHAR) NOT NULL,
    PORT          NUMBER(9),
    DATABASE_TYPE NUMBER(19),
    DESCRIPTION   VARCHAR2(255 CHAR),
    CREATED_AT    TIMESTAMP(6),
    CREATED_BY    VARCHAR2(64 CHAR),
    UPDATED_AT    TIMESTAMP(6),
    UPDATED_BY    VARCHAR2(64 CHAR),
    DELETED_AT    TIMESTAMP(6),
    DELETED_BY    VARCHAR2(64 CHAR),
    AUD_ACTION    VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE    TIMESTAMP(6)       NOT NULL,
    AUDIT_USER    VARCHAR2(64 CHAR)
);

CREATE TABLE INV_APP_INFORMATION_SYSTEM_DB
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

COMMENT ON TABLE INV_APP_INFORMATION_SYSTEM_DB IS 'Intersection table mapping corporate applications to their respective host systems';
COMMENT ON COLUMN INV_APP_INFORMATION_SYSTEM_DB.ID IS 'Unique system relation tracking primary key';
COMMENT ON COLUMN INV_APP_INFORMATION_SYSTEM_DB.APPLICATION_ID IS 'Foreign key referencing the parent corporate application';
COMMENT ON COLUMN INV_APP_INFORMATION_SYSTEM_DB.OBSERVATION IS 'Additional comments or observations';
COMMENT ON COLUMN INV_APP_INFORMATION_SYSTEM_DB.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_INFORMATION_SYSTEM_DB.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_INFORMATION_SYSTEM_DB.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_INFORMATION_SYSTEM_DB.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_INFORMATION_SYSTEM_DB.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_INFORMATION_SYSTEM_DB.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_INFORMATION_SYSTEM_DB_AUD
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

CREATE TABLE INV_APP_SYSTEM
(
    ID                    NUMBER(19)        NOT NULL,
    INFORMATION_SYSTEM_DB NUMBER(19)        NOT NULL,
    SYSTEM_ID             NUMBER(19)        NOT NULL,
    CREATED_AT            TIMESTAMP(6)      NOT NULL,
    CREATED_BY            VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT            TIMESTAMP(6),
    UPDATED_BY            VARCHAR2(64 CHAR),
    DELETED_AT            TIMESTAMP(6),
    DELETED_BY            VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_SYSTEM IS 'Intersection table mapping corporate applications to their respective host systems';
COMMENT ON COLUMN INV_APP_SYSTEM.ID IS 'Unique system relation tracking primary key';
COMMENT ON COLUMN INV_APP_SYSTEM.INFORMATION_SYSTEM_DB IS 'Foreign key referencing the parent corporate application relation';
COMMENT ON COLUMN INV_APP_SYSTEM.SYSTEM_ID IS 'Foreign key referencing the target host system';
COMMENT ON COLUMN INV_APP_SYSTEM.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_SYSTEM.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_SYSTEM.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_SYSTEM.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_SYSTEM.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_SYSTEM.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_SYSTEM_AUD
(
    AUDIT_ID              NUMBER(19)        NOT NULL,
    APP_SYSTEM_ID         NUMBER(19)        NOT NULL,
    INFORMATION_SYSTEM_DB NUMBER(19)        NOT NULL,
    SYSTEM_ID             NUMBER(19)        NOT NULL,
    CREATED_AT            TIMESTAMP(6),
    CREATED_BY            VARCHAR2(64 CHAR),
    UPDATED_AT            TIMESTAMP(6),
    UPDATED_BY            VARCHAR2(64 CHAR),
    DELETED_AT            TIMESTAMP(6),
    DELETED_BY            VARCHAR2(64 CHAR),
    AUD_ACTION            VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE            TIMESTAMP(6)      NOT NULL,
    AUDIT_USER            VARCHAR2(64 CHAR)
);

CREATE TABLE INV_APP_DATABASE
(
    ID                    NUMBER(19)        NOT NULL,
    INFORMATION_SYSTEM_DB NUMBER(19)        NOT NULL,
    DATABASE_ID           NUMBER(19)        NOT NULL,
    CREATED_AT            TIMESTAMP(6)      NOT NULL,
    CREATED_BY            VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT            TIMESTAMP(6),
    UPDATED_BY            VARCHAR2(64 CHAR),
    DELETED_AT            TIMESTAMP(6),
    DELETED_BY            VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_DATABASE IS 'Intersection table mapping corporate applications to database endpoints';
COMMENT ON COLUMN INV_APP_DATABASE.ID IS 'Unique database relation tracking primary key';
COMMENT ON COLUMN INV_APP_DATABASE.INFORMATION_SYSTEM_DB IS 'Foreign key referencing the parent corporate application relation';
COMMENT ON COLUMN INV_APP_DATABASE.DATABASE_ID IS 'Foreign key referencing the target database engine';
COMMENT ON COLUMN INV_APP_DATABASE.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_DATABASE.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_DATABASE.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_DATABASE.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_DATABASE.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_DATABASE.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_DATABASE_AUD
(
    AUDIT_ID              NUMBER(19)        NOT NULL,
    APP_DATABASE_ID       NUMBER(19)        NOT NULL,
    INFORMATION_SYSTEM_DB NUMBER(19)        NOT NULL,
    DATABASE_ID           NUMBER(19)        NOT NULL,
    CREATED_AT            TIMESTAMP(6),
    CREATED_BY            VARCHAR2(64 CHAR),
    UPDATED_AT            TIMESTAMP(6),
    UPDATED_BY            VARCHAR2(64 CHAR),
    DELETED_AT            TIMESTAMP(6),
    DELETED_BY            VARCHAR2(64 CHAR),
    AUD_ACTION            VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE            TIMESTAMP(6)      NOT NULL,
    AUDIT_USER            VARCHAR2(64 CHAR)
);

CREATE TABLE INV_LKUP_MODALITY
(
    ID      NUMBER(19)         NOT NULL,
    NAME    VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES VARCHAR2(100 CHAR) NOT NULL
);

COMMENT ON TABLE INV_LKUP_MODALITY IS 'Lookup table for development modality types';
COMMENT ON COLUMN INV_LKUP_MODALITY.ID IS 'Unique surrogate key for modality lookup entry';
COMMENT ON COLUMN INV_LKUP_MODALITY.NAME IS 'Catalan translation descriptive name for modality';
COMMENT ON COLUMN INV_LKUP_MODALITY.NAME_ES IS 'Spanish translation descriptive name for modality';

CREATE TABLE INV_LKUP_STANDARD_ADAPTION
(
    ID      NUMBER(19)         NOT NULL,
    NAME    VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES VARCHAR2(100 CHAR) NOT NULL
);

COMMENT ON TABLE INV_LKUP_STANDARD_ADAPTION IS 'Lookup table for GOIB standards compliance levels';
COMMENT ON COLUMN INV_LKUP_STANDARD_ADAPTION.ID IS 'Unique surrogate key for compliance lookup entry';
COMMENT ON COLUMN INV_LKUP_STANDARD_ADAPTION.NAME IS 'Catalan translation descriptive name for compliance level';
COMMENT ON COLUMN INV_LKUP_STANDARD_ADAPTION.NAME_ES IS 'Spanish translation descriptive name for compliance level';

CREATE TABLE INV_APP_DEVELOPMENT
(
    ID                NUMBER(19)        NOT NULL,
    APPLICATION_ID    NUMBER(19)        NOT NULL,
    ENVIRONMENT_ID    NUMBER(19)        ,
    MODALITY_ID       NUMBER(19),
    CODE              VARCHAR2(1000 CHAR),
    STANDARD_ADAPTION NUMBER(19),
    REVISION_DATE     TIMESTAMP(6),
    OBSERVATION       CLOB,
    CREATED_AT        TIMESTAMP(6)      NOT NULL,
    CREATED_BY        VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT        TIMESTAMP(6),
    UPDATED_BY        VARCHAR2(64 CHAR),
    DELETED_AT        TIMESTAMP(6),
    DELETED_BY        VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_DEVELOPMENT IS 'Main development module detail for application environments';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.ID IS 'Unique primary key for development record';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.APPLICATION_ID IS 'Foreign key referencing application';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.ENVIRONMENT_ID IS 'Foreign key referencing deployment environment';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.MODALITY_ID IS 'Foreign key referencing development modality lookup';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.CODE IS 'Source code repository URL';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.STANDARD_ADAPTION IS 'Foreign key referencing GOIB standards compliance lookup';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.REVISION_DATE IS 'Date of latest standards revision';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.OBSERVATION IS 'General rich-text OBSERVATION regarding development';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_DEVELOPMENT.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_DEVELOPMENT_AUD
(
    AUDIT_ID           NUMBER(19)        NOT NULL,
    APP_DEVELOPMENT_ID NUMBER(19)        NOT NULL,
    APPLICATION_ID     NUMBER(19)        NOT NULL,
    ENVIRONMENT_ID     NUMBER(19)        ,
    MODALITY_ID        NUMBER(19),
    CODE               VARCHAR2(1000 CHAR),
    STANDARD_ADAPTION  NUMBER(19),
    REVISION_DATE      TIMESTAMP(6),
    OBSERVATION        CLOB,
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

CREATE TABLE INV_ROLE
(
    ID         NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES    VARCHAR2(100 CHAR),
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_ROLE IS 'Catalog table for appProvider roles assignable within application development modules';
COMMENT ON COLUMN INV_ROLE.ID IS 'Unique identifier for the role';
COMMENT ON COLUMN INV_ROLE.NAME IS 'Catalan translation descriptive name for the role';
COMMENT ON COLUMN INV_ROLE.NAME_ES IS 'Spanish translation descriptive name for the role';
COMMENT ON COLUMN INV_ROLE.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_ROLE.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_ROLE.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_ROLE.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_ROLE.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_ROLE.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_ROLE_AUD
(
    AUDIT_ID   NUMBER(19)         NOT NULL,
    ROLE_ID    NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES    VARCHAR2(100 CHAR),
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

CREATE TABLE INV_LAYER
(
    ID         NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(100 CHAR) NOT NULL,
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_LAYER IS 'Catalog table for architecture layers (e.g. Frontend, Backend) assignable to technologies';
COMMENT ON COLUMN INV_LAYER.ID IS 'Unique identifier for the architecture layer';
COMMENT ON COLUMN INV_LAYER.NAME IS 'Descriptive name of the architecture layer';
COMMENT ON COLUMN INV_LAYER.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_LAYER.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_LAYER.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_LAYER.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_LAYER.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_LAYER.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_LAYER_AUD
(
    AUDIT_ID   NUMBER(19)         NOT NULL,
    LAYER_ID   NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(100 CHAR) NOT NULL,
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

CREATE TABLE INV_TECHNOLOGY
(
    ID         NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(100 CHAR) NOT NULL,
    LAYER      NUMBER(19)         NOT NULL,
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_TECHNOLOGY IS 'Catalog table for technologies (e.g. React, Spring) organized under an architecture layer';
COMMENT ON COLUMN INV_TECHNOLOGY.ID IS 'Unique identifier for the technology';
COMMENT ON COLUMN INV_TECHNOLOGY.NAME IS 'Descriptive name of the technology';
COMMENT ON COLUMN INV_TECHNOLOGY.LAYER IS 'Foreign key referencing the architecture layer catalog (INV_LAYER)';
COMMENT ON COLUMN INV_TECHNOLOGY.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_TECHNOLOGY.CREATED_BY IS 'Audit identifier of the user or system process that created the record';
COMMENT ON COLUMN INV_TECHNOLOGY.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_TECHNOLOGY.UPDATED_BY IS 'Audit identifier of the user who performed the latest update';
COMMENT ON COLUMN INV_TECHNOLOGY.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_TECHNOLOGY.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_TECHNOLOGY_AUD
(
    AUDIT_ID      NUMBER(19)         NOT NULL,
    TECHNOLOGY_ID NUMBER(19)         NOT NULL,
    NAME          VARCHAR2(100 CHAR) NOT NULL,
    LAYER_ID      NUMBER(19)         NOT NULL,
    CREATED_AT    TIMESTAMP(6),
    CREATED_BY    VARCHAR2(64 CHAR),
    UPDATED_AT    TIMESTAMP(6),
    UPDATED_BY    VARCHAR2(64 CHAR),
    DELETED_AT    TIMESTAMP(6),
    DELETED_BY    VARCHAR2(64 CHAR),
    AUD_ACTION    VARCHAR2(10 CHAR)  NOT NULL,
    AUDIT_DATE    TIMESTAMP(6)       NOT NULL,
    AUDIT_USER    VARCHAR2(64 CHAR)
);

CREATE TABLE INV_APP_PROVIDER
(
    ID                 NUMBER(19)         NOT NULL,
    APP_DEVELOPMENT_ID NUMBER(19)         NOT NULL,
    COMPANY_NAME       VARCHAR2(255 CHAR) NOT NULL,
    ROLE               NUMBER(19),
    START_DATE         TIMESTAMP(6),
    EXPIRE_DATE        TIMESTAMP(6),
    CREATED_AT         TIMESTAMP(6)       NOT NULL,
    CREATED_BY         VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(64 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_PROVIDER IS 'Auxiliary table mapping providers to application development modules';
COMMENT ON COLUMN INV_APP_PROVIDER.ID IS 'Unique primary key for appProvider assignment';
COMMENT ON COLUMN INV_APP_PROVIDER.APP_DEVELOPMENT_ID IS 'Foreign key referencing development module entry';
COMMENT ON COLUMN INV_APP_PROVIDER.COMPANY_NAME IS 'Company corporate name';
COMMENT ON COLUMN INV_APP_PROVIDER.ROLE IS 'Foreign key referencing the role catalog (INV_ROLE) played by the appProvider';
COMMENT ON COLUMN INV_APP_PROVIDER.START_DATE IS 'Start date for appProvider service contract';
COMMENT ON COLUMN INV_APP_PROVIDER.EXPIRE_DATE IS 'End date for appProvider service contract';
COMMENT ON COLUMN INV_APP_PROVIDER.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_PROVIDER.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_PROVIDER.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_PROVIDER.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_PROVIDER.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_PROVIDER.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_PROVIDER_AUD
(
    AUDIT_ID           NUMBER(19)         NOT NULL,
    APP_PROVIDER_ID    NUMBER(19)         NOT NULL,
    APP_DEVELOPMENT_ID NUMBER(19)         NOT NULL,
    COMPANY_NAME       VARCHAR2(255 CHAR) NOT NULL,
    ROLE               NUMBER(19),
    START_DATE         TIMESTAMP(6),
    EXPIRE_DATE        TIMESTAMP(6),
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

CREATE TABLE INV_APP_TECHNOLOGY
(
    ID                 NUMBER(19)        NOT NULL,
    APP_DEVELOPMENT_ID NUMBER(19)        NOT NULL,
    LAYER              NUMBER(19)        NOT NULL,
    TECHNOLOGY         NUMBER(19)        NOT NULL,
    VERSION            VARCHAR2(255 CHAR),
    ARCHITECTURE       VARCHAR2(255 CHAR),
    CREATED_AT         TIMESTAMP(6)      NOT NULL,
    CREATED_BY         VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(64 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_APP_TECHNOLOGY IS 'Auxiliary table mapping technologies to application development modules';
COMMENT ON COLUMN INV_APP_TECHNOLOGY.ID IS 'Unique primary key for technology entry';
COMMENT ON COLUMN INV_APP_TECHNOLOGY.APP_DEVELOPMENT_ID IS 'Foreign key referencing development module entry';
COMMENT ON COLUMN INV_APP_TECHNOLOGY.LAYER IS 'Foreign key referencing the architecture layer catalog (INV_LAYER)';
COMMENT ON COLUMN INV_APP_TECHNOLOGY.TECHNOLOGY IS 'Foreign key referencing the technology catalog (INV_TECHNOLOGY)';
COMMENT ON COLUMN INV_APP_TECHNOLOGY.VERSION IS 'Version release details';
COMMENT ON COLUMN INV_APP_TECHNOLOGY.ARCHITECTURE IS 'Architectural model description';
COMMENT ON COLUMN INV_APP_TECHNOLOGY.CREATED_AT IS 'Audit timestamp registering record creation';
COMMENT ON COLUMN INV_APP_TECHNOLOGY.CREATED_BY IS 'Audit identifier of the user who created the record';
COMMENT ON COLUMN INV_APP_TECHNOLOGY.UPDATED_AT IS 'Audit timestamp registering latest data update';
COMMENT ON COLUMN INV_APP_TECHNOLOGY.UPDATED_BY IS 'Audit identifier of the user who updated the record';
COMMENT ON COLUMN INV_APP_TECHNOLOGY.DELETED_AT IS 'Audit timestamp registering record soft-deletion';
COMMENT ON COLUMN INV_APP_TECHNOLOGY.DELETED_BY IS 'Audit identifier of the user who soft-deleted the record';

CREATE TABLE INV_APP_TECHNOLOGY_AUD
(
    AUDIT_ID           NUMBER(19)        NOT NULL,
    APP_TECHNOLOGY_ID  NUMBER(19)        NOT NULL,
    APP_DEVELOPMENT_ID NUMBER(19)        NOT NULL,
    LAYER              NUMBER(19)        NOT NULL,
    TECHNOLOGY         NUMBER(19)        NOT NULL,
    VERSION            VARCHAR2(255 CHAR),
    ARCHITECTURE       VARCHAR2(255 CHAR),
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