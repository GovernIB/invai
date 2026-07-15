CREATE TABLE INV_CATEGORY
(
    CATEGORY_ID NUMBER(19)         NOT NULL,
    NAME        VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES     VARCHAR2(100 CHAR) NOT NULL,
    CREATED_AT  TIMESTAMP(6)       NOT NULL,
    CREATED_BY  VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT  TIMESTAMP(6),
    UPDATED_BY  VARCHAR2(64 CHAR),
    DELETED_AT  TIMESTAMP(6),
    DELETED_BY  VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_CATEGORY IS 'Categories master table';
COMMENT ON COLUMN INV_CATEGORY.CATEGORY_ID IS 'Identifier of the inventory category';
COMMENT ON COLUMN INV_CATEGORY.NAME IS 'Name description of the inventory category (Catalan/General)';
COMMENT ON COLUMN INV_CATEGORY.NAME_ES IS 'Name description of the inventory category (Spanish translation)';
COMMENT ON COLUMN INV_CATEGORY.CREATED_AT IS 'Timestamp tracking row baseline initialization';
COMMENT ON COLUMN INV_CATEGORY.CREATED_BY IS 'User capturing row insertion context';
COMMENT ON COLUMN INV_CATEGORY.UPDATED_AT IS 'Timestamp tracking last structural row modification';
COMMENT ON COLUMN INV_CATEGORY.UPDATED_BY IS 'User signature metadata tracking updates';
COMMENT ON COLUMN INV_CATEGORY.DELETED_AT IS 'Timestamp tracking logical deletion/deactivation milestone';
COMMENT ON COLUMN INV_CATEGORY.DELETED_BY IS 'User signature capturing the logical delete action';

CREATE TABLE INV_CATEGORY_AUD
(
    AUDIT_ID    NUMBER(19)         NOT NULL,
    CATEGORY_ID NUMBER(19)         NOT NULL,
    NAME        VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES     VARCHAR2(100 CHAR) NOT NULL,
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

COMMENT ON COLUMN INV_CATEGORY_AUD.AUDIT_ID IS 'Unique serial primary tracking key for the audit record logs';

CREATE TABLE INV_SYSTEM_TYPE
(
    SYSTEM_TYPE_ID NUMBER(19)         NOT NULL,
    NAME           VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES        VARCHAR2(100 CHAR) NOT NULL,
    CREATED_AT     TIMESTAMP(6)       NOT NULL,
    CREATED_BY     VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT     TIMESTAMP(6),
    UPDATED_BY     VARCHAR2(64 CHAR),
    DELETED_AT     TIMESTAMP(6),
    DELETED_BY     VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_SYSTEM_TYPE IS 'System types master table';
COMMENT ON COLUMN INV_SYSTEM_TYPE.SYSTEM_TYPE_ID IS 'Identifier of the inventory system type';
COMMENT ON COLUMN INV_SYSTEM_TYPE.NAME IS 'Name description of the inventory system type (Catalan/General)';
COMMENT ON COLUMN INV_SYSTEM_TYPE.NAME_ES IS 'Name description of the inventory system type (Spanish translation)';
COMMENT ON COLUMN INV_SYSTEM_TYPE.CREATED_AT IS 'Timestamp tracking row baseline initialization';
COMMENT ON COLUMN INV_SYSTEM_TYPE.CREATED_BY IS 'User capturing row insertion context';
COMMENT ON COLUMN INV_SYSTEM_TYPE.UPDATED_AT IS 'Timestamp tracking last structural row modification';
COMMENT ON COLUMN INV_SYSTEM_TYPE.UPDATED_BY IS 'User signature metadata tracking updates';
COMMENT ON COLUMN INV_SYSTEM_TYPE.DELETED_AT IS 'Timestamp tracking logical deletion/deactivation milestone';
COMMENT ON COLUMN INV_SYSTEM_TYPE.DELETED_BY IS 'User signature capturing the logical delete action';

CREATE TABLE INV_SYSTEM_TYPE_AUD
(
    AUDIT_ID       NUMBER(19)         NOT NULL,
    SYSTEM_TYPE_ID NUMBER(19)         NOT NULL,
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

CREATE TABLE INV_LKUP_STATUS
(
    STATUS_ID NUMBER(19)        NOT NULL,
    CODE      VARCHAR2(64 CHAR) NOT NULL,
    NAME      VARCHAR2(64 CHAR) NOT NULL,
    NAME_ES   VARCHAR2(64 CHAR) NOT NULL
);

COMMENT ON TABLE INV_LKUP_STATUS IS 'Lookup table mapping core status codes to Catalan and Spanish names';
COMMENT ON COLUMN INV_LKUP_STATUS.STATUS_ID IS 'Unique surrogate key for the status translation entry';
COMMENT ON COLUMN INV_LKUP_STATUS.CODE IS 'Standardized technical code for the status (e.g., ACTIVE)';
COMMENT ON COLUMN INV_LKUP_STATUS.NAME IS 'Catalan translation descriptive name for the status';
COMMENT ON COLUMN INV_LKUP_STATUS.NAME_ES IS 'Spanish translation descriptive name for the status';

CREATE TABLE INV_FIELD
(
    FIELD_ID   NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES    VARCHAR2(100 CHAR) NOT NULL,
    CREATED_AT TIMESTAMP(6)       NOT NULL,
    CREATED_BY VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_FIELD IS 'Functional fields master table';
COMMENT ON COLUMN INV_FIELD.FIELD_ID IS 'Identifier of the inventory field zone';
COMMENT ON COLUMN INV_FIELD.NAME IS 'Name description of the inventory field zone (Catalan/General)';
COMMENT ON COLUMN INV_FIELD.NAME_ES IS 'Name description of the inventory field zone (Spanish translation)';
COMMENT ON COLUMN INV_FIELD.CREATED_AT IS 'Timestamp tracking row baseline initialization';
COMMENT ON COLUMN INV_FIELD.CREATED_BY IS 'User capturing row insertion context';
COMMENT ON COLUMN INV_FIELD.UPDATED_AT IS 'Timestamp tracking last structural row modification';
COMMENT ON COLUMN INV_FIELD.UPDATED_BY IS 'User signature metadata tracking updates';
COMMENT ON COLUMN INV_FIELD.DELETED_AT IS 'Timestamp tracking logical deletion/deactivation milestone';
COMMENT ON COLUMN INV_FIELD.DELETED_BY IS 'User signature capturing the logical delete action';

CREATE TABLE INV_FIELD_AUD
(
    AUDIT_ID   NUMBER(19)         NOT NULL,
    FIELD_ID   NUMBER(19)         NOT NULL,
    NAME       VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES    VARCHAR2(100 CHAR) NOT NULL,
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

CREATE TABLE INV_ADM_UNIT
(
    ADM_UNIT_ID NUMBER(19)         NOT NULL,
    CODE        VARCHAR2(7 CHAR)   NOT NULL,
    NAME        VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES     VARCHAR2(100 CHAR) NOT NULL,
    CREATED_AT  TIMESTAMP(6)       NOT NULL,
    CREATED_BY  VARCHAR2(64 CHAR)  NOT NULL,
    UPDATED_AT  TIMESTAMP(6),
    UPDATED_BY  VARCHAR2(64 CHAR),
    DELETED_AT  TIMESTAMP(6),
    DELETED_BY  VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_ADM_UNIT IS 'Administrative units master table';
COMMENT ON COLUMN INV_ADM_UNIT.ADM_UNIT_ID IS 'Identifier of the inventory administrative unit';
COMMENT ON COLUMN INV_ADM_UNIT.CODE IS 'Corporate structural code of the administrative unit';
COMMENT ON COLUMN INV_ADM_UNIT.NAME IS 'Descriptive name of the administrative unit (Catalan/General)';
COMMENT ON COLUMN INV_ADM_UNIT.NAME_ES IS 'Descriptive name of the administrative unit (Spanish translation)';
COMMENT ON COLUMN INV_ADM_UNIT.CREATED_AT IS 'Timestamp tracking row baseline initialization';
COMMENT ON COLUMN INV_ADM_UNIT.CREATED_BY IS 'User capturing row insertion context';
COMMENT ON COLUMN INV_ADM_UNIT.UPDATED_AT IS 'Timestamp tracking last structural row modification';
COMMENT ON COLUMN INV_ADM_UNIT.UPDATED_BY IS 'User signature metadata tracking updates';
COMMENT ON COLUMN INV_ADM_UNIT.DELETED_AT IS 'Timestamp tracking logical deletion/deactivation milestone';
COMMENT ON COLUMN INV_ADM_UNIT.DELETED_BY IS 'User signature capturing the logical delete action';

CREATE TABLE INV_ADM_UNIT_AUD
(
    AUDIT_ID    NUMBER(19)         NOT NULL,
    ADM_UNIT_ID NUMBER(19)         NOT NULL,
    CODE        VARCHAR2(7 CHAR)   NOT NULL,
    NAME        VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES     VARCHAR2(100 CHAR) NOT NULL,
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

CREATE TABLE INV_COMMISSION
(
    COMMISSION_ID    NUMBER(19)        NOT NULL,
    NAME             VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES          VARCHAR2(100 CHAR) NOT NULL,
    EXPEDIENT_NUMBER VARCHAR2(64 CHAR) NOT NULL,
    APPROVAL_DATE    DATE              NOT NULL,
    COMMISSION_TYPE  VARCHAR2(20 CHAR) NOT NULL,
    CREATED_AT       TIMESTAMP(6)      NOT NULL,
    CREATED_BY       VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT       TIMESTAMP(6),
    UPDATED_BY       VARCHAR2(64 CHAR),
    DELETED_AT       TIMESTAMP(6),
    DELETED_BY       VARCHAR2(64 CHAR)
);

COMMENT ON TABLE INV_COMMISSION IS 'Commission entities master table including approval governance details';
COMMENT ON COLUMN INV_COMMISSION.COMMISSION_ID IS 'Identifier of the inventory commission entity';
COMMENT ON COLUMN INV_COMMISSION.NAME IS 'Descriptive name of the commission entity (Catalan/General)';
COMMENT ON COLUMN INV_COMMISSION.NAME_ES IS 'Descriptive name of the commission entity (Spanish translation)';
COMMENT ON COLUMN INV_COMMISSION.EXPEDIENT_NUMBER IS 'Formal expedient tracking number for the commission approval';
COMMENT ON COLUMN INV_COMMISSION.APPROVAL_DATE IS 'Official approval date milestone for the commission';
COMMENT ON COLUMN INV_COMMISSION.COMMISSION_TYPE IS 'Classification level of the commission (TECNICA or SUPERIOR)';
COMMENT ON COLUMN INV_COMMISSION.CREATED_AT IS 'Timestamp tracking row baseline initialization';
COMMENT ON COLUMN INV_COMMISSION.CREATED_BY IS 'User capturing row insertion context';
COMMENT ON COLUMN INV_COMMISSION.UPDATED_AT IS 'Timestamp tracking last structural row modification';
COMMENT ON COLUMN INV_COMMISSION.UPDATED_BY IS 'User signature metadata tracking updates';
COMMENT ON COLUMN INV_COMMISSION.DELETED_AT IS 'Timestamp tracking logical deletion/deactivation milestone';
COMMENT ON COLUMN INV_COMMISSION.DELETED_BY IS 'User signature capturing the logical delete action';

CREATE TABLE INV_COMMISSION_AUD
(
    AUDIT_ID         NUMBER(19)        NOT NULL,
    COMMISSION_ID    NUMBER(19)        NOT NULL,
    NAME             VARCHAR2(100 CHAR) NOT NULL,
    NAME_ES          VARCHAR2(100 CHAR) NOT NULL,
    EXPEDIENT_NUMBER VARCHAR2(64 CHAR),
    APPROVAL_DATE    DATE,
    COMMISSION_TYPE  VARCHAR2(20 CHAR),
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

COMMENT ON COLUMN INV_COMMISSION_AUD.EXPEDIENT_NUMBER IS 'Formal expedient tracking number audited';
COMMENT ON COLUMN INV_COMMISSION_AUD.APPROVAL_DATE IS 'Official approval date milestone audited';
COMMENT ON COLUMN INV_COMMISSION_AUD.COMMISSION_TYPE IS 'Classification level of the commission audited';

CREATE TABLE INV_APPLICATION
(
    APP_APPLICATION_ID NUMBER(19)        NOT NULL,
    CODE               VARCHAR2(10 CHAR) NOT NULL,
    PREFIX             VARCHAR2(3 CHAR)  NOT NULL,
    NAME               VARCHAR2(255 CHAR),
    CATEGORY_ID        NUMBER(19),
    SYSTEM_TYPE_ID     NUMBER(19),
    FIELD_ID           NUMBER(19),
    ADM_UNIT_ID        NUMBER(19),
    COMMISSION_ID      NUMBER(19),
    STATUS_ID          NUMBER(19)        NOT NULL,
    DESCRIPTION        CLOB,
    CREATED_AT         TIMESTAMP(6)      NOT NULL,
    CREATED_BY         VARCHAR2(64 CHAR) NOT NULL,
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(64 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR),
    EXPIRATION_DATE    TIMESTAMP(6)
);

COMMENT ON TABLE INV_APPLICATION IS 'Core inventory application registry table';
COMMENT ON COLUMN INV_APPLICATION.APP_APPLICATION_ID IS 'Unique identifier for the inventory application';
COMMENT ON COLUMN INV_APPLICATION.CODE IS 'Functional corporate code of the application';
COMMENT ON COLUMN INV_APPLICATION.PREFIX IS 'Three-letter architectural prefix string';
COMMENT ON COLUMN INV_APPLICATION.NAME IS 'Full name descriptive of the application';
COMMENT ON COLUMN INV_APPLICATION.CATEGORY_ID IS 'Foreign Key referencing the master category entity';
COMMENT ON COLUMN INV_APPLICATION.SYSTEM_TYPE_ID IS 'Foreign Key referencing the master system type entity';
COMMENT ON COLUMN INV_APPLICATION.FIELD_ID IS 'Foreign Key referencing the master sector field entity';
COMMENT ON COLUMN INV_APPLICATION.ADM_UNIT_ID IS 'Foreign Key referencing the master administrative unit entity';
COMMENT ON COLUMN INV_APPLICATION.COMMISSION_ID IS 'Foreign Key referencing the master commission entity';
COMMENT ON COLUMN INV_APPLICATION.STATUS_ID IS 'Foreign Key referencing the master execution lifecycle status';
COMMENT ON COLUMN INV_APPLICATION.DESCRIPTION IS 'Detailed extended functional log text context';
COMMENT ON COLUMN INV_APPLICATION.CREATED_AT IS 'Timestamp tracking row baseline initialization';
COMMENT ON COLUMN INV_APPLICATION.CREATED_BY IS 'User capturing row insertion context';
COMMENT ON COLUMN INV_APPLICATION.UPDATED_AT IS 'Timestamp tracking last structural row modification';
COMMENT ON COLUMN INV_APPLICATION.UPDATED_BY IS 'User signature metadata tracking updates';
COMMENT ON COLUMN INV_APPLICATION.DELETED_AT IS 'Timestamp tracking logical deletion/deactivation milestone';
COMMENT ON COLUMN INV_APPLICATION.DELETED_BY IS 'User signature capturing the logical delete action';
COMMENT ON COLUMN INV_APPLICATION.EXPIRATION_DATE IS 'Validity limit deadline timestamp milestone';

CREATE TABLE INV_APPLICATION_AUD
(
    AUDIT_ID           NUMBER(19)        NOT NULL,
    APP_APPLICATION_ID NUMBER(19)        NOT NULL,
    CODE               VARCHAR2(10 CHAR) NOT NULL,
    PREFIX             VARCHAR2(3 CHAR)  NOT NULL,
    NAME               VARCHAR2(255 CHAR),
    CATEGORY_ID        NUMBER(19),
    SYSTEM_TYPE_ID     NUMBER(19),
    FIELD_ID           NUMBER(19),
    ADM_UNIT_ID        NUMBER(19),
    COMMISSION_ID      NUMBER(19),
    STATUS_ID          NUMBER(19)        NOT NULL,
    DESCRIPTION        CLOB,
    CREATED_AT         TIMESTAMP(6),
    CREATED_BY         VARCHAR2(64 CHAR),
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(64 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR),
    EXPIRATION_DATE    TIMESTAMP(6),
    AUD_ACTION         VARCHAR2(10 CHAR) NOT NULL,
    AUDIT_DATE         TIMESTAMP(6)      NOT NULL,
    AUDIT_USER         VARCHAR2(64 CHAR)
);