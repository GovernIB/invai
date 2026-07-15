create table INV_CATEGORY
(
    CATEGORY_ID NUMBER(19)        not null,
    NAME        VARCHAR2(50 CHAR) not null,
    NAME_ES     VARCHAR2(50 CHAR) not null,
    CREATED_AT  TIMESTAMP(6)      not null,
    CREATED_BY  VARCHAR2(64 CHAR) not null,
    UPDATED_AT  TIMESTAMP(6),
    UPDATED_BY  VARCHAR2(64 CHAR),
    DELETED_AT  TIMESTAMP(6),
    DELETED_BY  VARCHAR2(64 CHAR)
);

comment on column INV_CATEGORY.CATEGORY_ID is 'Identifier of the inventory category';
comment on column INV_CATEGORY.NAME is 'Name description of the inventory category (Catalan/General)';
comment on column INV_CATEGORY.NAME_ES is 'Name description of the inventory category (Spanish translation)';
comment on column INV_CATEGORY.CREATED_AT is 'Timestamp tracking row baseline initialization';
comment on column INV_CATEGORY.CREATED_BY is 'User capturing row insertion context';
comment on column INV_CATEGORY.UPDATED_AT is 'Timestamp tracking last structural row modification';
comment on column INV_CATEGORY.UPDATED_BY is 'User signature metadata tracking updates';
comment on column INV_CATEGORY.DELETED_AT is 'Timestamp tracking logical deletion/deactivation milestone';
comment on column INV_CATEGORY.DELETED_BY is 'User signature capturing the logical delete action';

create table INV_CATEGORY_AUD
(
    AUDIT_ID    NUMBER(19)        not null,
    CATEGORY_ID NUMBER(19)        not null,
    NAME        VARCHAR2(50 CHAR) not null,
    NAME_ES     VARCHAR2(50 CHAR) not null,
    CREATED_AT  TIMESTAMP(6),
    CREATED_BY  VARCHAR2(64 CHAR),
    UPDATED_AT  TIMESTAMP(6),
    UPDATED_BY  VARCHAR2(64 CHAR),
    DELETED_AT  TIMESTAMP(6),
    DELETED_BY  VARCHAR2(64 CHAR),
    AUD_ACTION  VARCHAR2(10 CHAR) not null,
    AUDIT_DATE  TIMESTAMP(6)      not null,
    AUDIT_USER  VARCHAR2(64 CHAR)
);

comment on column INV_CATEGORY_AUD.AUDIT_ID is 'Unique serial primary tracking key for the audit record logs';

create table INV_SYSTEM_TYPE
(
    SYSTEM_TYPE_ID NUMBER(19)        not null,
    NAME           VARCHAR2(50 CHAR) not null,
    NAME_ES        VARCHAR2(50 CHAR) not null,
    CREATED_AT     TIMESTAMP(6)      not null,
    CREATED_BY     VARCHAR2(64 CHAR) not null,
    UPDATED_AT     TIMESTAMP(6),
    UPDATED_BY     VARCHAR2(64 CHAR),
    DELETED_AT     TIMESTAMP(6),
    DELETED_BY     VARCHAR2(64 CHAR)
);

comment on column INV_SYSTEM_TYPE.SYSTEM_TYPE_ID is 'Identifier of the inventory system type';
comment on column INV_SYSTEM_TYPE.NAME is 'Name description of the inventory system type (Catalan/General)';
comment on column INV_SYSTEM_TYPE.NAME_ES is 'Name description of the inventory system type (Spanish translation)';
comment on column INV_SYSTEM_TYPE.CREATED_AT is 'Timestamp tracking row baseline initialization';
comment on column INV_SYSTEM_TYPE.CREATED_BY is 'User capturing row insertion context';
comment on column INV_SYSTEM_TYPE.UPDATED_AT is 'Timestamp tracking last structural row modification';
comment on column INV_SYSTEM_TYPE.UPDATED_BY is 'User signature metadata tracking updates';
comment on column INV_SYSTEM_TYPE.DELETED_AT is 'Timestamp tracking logical deletion/deactivation milestone';
comment on column INV_SYSTEM_TYPE.DELETED_BY is 'User signature capturing the logical delete action';

create table INV_SYSTEM_TYPE_AUD
(
    AUDIT_ID       NUMBER(19)        not null,
    SYSTEM_TYPE_ID NUMBER(19)        not null,
    NAME           VARCHAR2(50 CHAR) not null,
    NAME_ES        VARCHAR2(50 CHAR) not null,
    CREATED_AT     TIMESTAMP(6),
    CREATED_BY     VARCHAR2(64 CHAR),
    UPDATED_AT     TIMESTAMP(6),
    UPDATED_BY     VARCHAR2(64 CHAR),
    DELETED_AT     TIMESTAMP(6),
    DELETED_BY     VARCHAR2(64 CHAR),
    AUD_ACTION     VARCHAR2(10 CHAR) not null,
    AUDIT_DATE     TIMESTAMP(6)      not null,
    AUDIT_USER     VARCHAR2(64 CHAR)
);

create table INV_STATUS
(
    STATUS_ID  NUMBER(19)        not null,
    NAME       VARCHAR2(64 CHAR) not null,
    CREATED_AT TIMESTAMP(6)      not null,
    CREATED_BY VARCHAR2(64 CHAR) not null,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

comment on column INV_STATUS.NAME is 'Name description of the inventory status';
comment on column INV_STATUS.CREATED_AT is 'Timestamp tracking row baseline initialization';
comment on column INV_STATUS.CREATED_BY is 'User capturing row insertion context';
comment on column INV_STATUS.UPDATED_AT is 'Timestamp tracking last structural row modification';
comment on column INV_STATUS.UPDATED_BY is 'User signature metadata tracking updates';
comment on column INV_STATUS.DELETED_AT is 'Timestamp tracking logical deletion/deactivation milestone';
comment on column INV_STATUS.DELETED_BY is 'User signature capturing the logical delete action';

create table INV_STATUS_AUD
(
    AUDIT_ID   NUMBER(19)        not null,
    NAME       VARCHAR2(64 CHAR) not null,
    CREATED_AT TIMESTAMP(6),
    CREATED_BY VARCHAR2(64 CHAR),
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR),
    AUD_ACTION VARCHAR2(10 CHAR) not null,
    AUDIT_DATE TIMESTAMP(6)      not null,
    AUDIT_USER VARCHAR2(64 CHAR)
);


create table INV_FIELD
(
    FIELD_ID   NUMBER(19)        not null,
    NAME       VARCHAR2(50 CHAR) not null,
    NAME_ES    VARCHAR2(50 CHAR) not null,
    CREATED_AT TIMESTAMP(6)      not null,
    CREATED_BY VARCHAR2(64 CHAR) not null,
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR)
);

comment on column INV_FIELD.FIELD_ID is 'Identifier of the inventory field zone';
comment on column INV_FIELD.NAME is 'Name description of the inventory field zone (Catalan/General)';
comment on column INV_FIELD.NAME_ES is 'Name description of the inventory field zone (Spanish translation)';
comment on column INV_FIELD.CREATED_AT is 'Timestamp tracking row baseline initialization';
comment on column INV_FIELD.CREATED_BY is 'User capturing row insertion context';
comment on column INV_FIELD.UPDATED_AT is 'Timestamp tracking last structural row modification';
comment on column INV_FIELD.UPDATED_BY is 'User signature metadata tracking updates';
comment on column INV_FIELD.DELETED_AT is 'Timestamp tracking logical deletion/deactivation milestone';
comment on column INV_FIELD.DELETED_BY is 'User signature capturing the logical delete action';

create table INV_FIELD_AUD
(
    AUDIT_ID   NUMBER(19)        not null,
    FIELD_ID   NUMBER(19)        not null,
    NAME       VARCHAR2(50 CHAR) not null,
    NAME_ES    VARCHAR2(50 CHAR) not null,
    CREATED_AT TIMESTAMP(6),
    CREATED_BY VARCHAR2(64 CHAR),
    UPDATED_AT TIMESTAMP(6),
    UPDATED_BY VARCHAR2(64 CHAR),
    DELETED_AT TIMESTAMP(6),
    DELETED_BY VARCHAR2(64 CHAR),
    AUD_ACTION VARCHAR2(10 CHAR) not null,
    AUDIT_DATE TIMESTAMP(6)      not null,
    AUDIT_USER VARCHAR2(64 CHAR)
);

create table INV_ADM_UNIT
(
    ADM_UNIT_ID NUMBER(19)        not null,
    CODE        VARCHAR2(7 CHAR),
    NAME        VARCHAR2(100 CHAR),
    NAME_ES     VARCHAR2(100 CHAR),
    CREATED_AT  TIMESTAMP(6)      not null,
    CREATED_BY  VARCHAR2(64 CHAR) not null,
    UPDATED_AT  TIMESTAMP(6),
    UPDATED_BY  VARCHAR2(64 CHAR),
    DELETED_AT  TIMESTAMP(6),
    DELETED_BY  VARCHAR2(64 CHAR)
);

comment on column INV_ADM_UNIT.ADM_UNIT_ID is 'Identifier of the inventory administrative unit';
comment on column INV_ADM_UNIT.CODE is 'Corporate structural code of the administrative unit';
comment on column INV_ADM_UNIT.NAME is 'Descriptive name of the administrative unit (Catalan/General)';
comment on column INV_ADM_UNIT.NAME_ES is 'Descriptive name of the administrative unit (Spanish translation)';
comment on column INV_ADM_UNIT.CREATED_AT is 'Timestamp tracking row baseline initialization';
comment on column INV_ADM_UNIT.CREATED_BY is 'User capturing row insertion context';
comment on column INV_ADM_UNIT.UPDATED_AT is 'Timestamp tracking last structural row modification';
comment on column INV_ADM_UNIT.UPDATED_BY is 'User signature metadata tracking updates';
comment on column INV_ADM_UNIT.DELETED_AT is 'Timestamp tracking logical deletion/deactivation milestone';
comment on column INV_ADM_UNIT.DELETED_BY is 'User signature capturing the logical delete action';

create table INV_ADM_UNIT_AUD
(
    AUDIT_ID    NUMBER(19)        not null,
    ADM_UNIT_ID NUMBER(19)        not null,
    CODE        VARCHAR2(7 CHAR),
    NAME        VARCHAR2(100 CHAR),
    NAME_ES     VARCHAR2(100 CHAR),
    CREATED_AT  TIMESTAMP(6),
    CREATED_BY  VARCHAR2(64 CHAR),
    UPDATED_AT  TIMESTAMP(6),
    UPDATED_BY  VARCHAR2(64 CHAR),
    DELETED_AT  TIMESTAMP(6),
    DELETED_BY  VARCHAR2(64 CHAR),
    AUD_ACTION  VARCHAR2(10 CHAR) not null,
    AUDIT_DATE  TIMESTAMP(6)      not null,
    AUDIT_USER  VARCHAR2(64 CHAR)
);

create table INV_COMMISSION
(
    COMMISSION_ID NUMBER(19)        not null,
    NAME          VARCHAR2(100 CHAR),
    NAME_ES       VARCHAR2(100 CHAR),
    CREATED_AT    TIMESTAMP(6)      not null,
    CREATED_BY    VARCHAR2(64 CHAR) not null,
    UPDATED_AT    TIMESTAMP(6),
    UPDATED_BY    VARCHAR2(64 CHAR),
    DELETED_AT    TIMESTAMP(6),
    DELETED_BY    VARCHAR2(64 CHAR)
);

comment on column INV_COMMISSION.COMMISSION_ID is 'Identifier of the inventory commission entity';
comment on column INV_COMMISSION.NAME is 'Descriptive name of the commission entity (Catalan/General)';
comment on column INV_COMMISSION.NAME_ES is 'Descriptive name of the commission entity (Spanish translation)';
comment on column INV_COMMISSION.CREATED_AT is 'Timestamp tracking row baseline initialization';
comment on column INV_COMMISSION.CREATED_BY is 'User capturing row insertion context';
comment on column INV_COMMISSION.UPDATED_AT is 'Timestamp tracking last structural row modification';
comment on column INV_COMMISSION.UPDATED_BY is 'User signature metadata tracking updates';
comment on column INV_COMMISSION.DELETED_AT is 'Timestamp tracking logical deletion/deactivation milestone';
comment on column INV_COMMISSION.DELETED_BY is 'User signature capturing the logical delete action';

create table INV_COMMISSION_AUD
(
    AUDIT_ID      NUMBER(19)        not null,
    COMMISSION_ID NUMBER(19)        not null,
    NAME          VARCHAR2(100 CHAR),
    NAME_ES       VARCHAR2(100 CHAR),
    CREATED_AT    TIMESTAMP(6),
    CREATED_BY    VARCHAR2(64 CHAR),
    UPDATED_AT    TIMESTAMP(6),
    UPDATED_BY    VARCHAR2(64 CHAR),
    DELETED_AT    TIMESTAMP(6),
    DELETED_BY    VARCHAR2(64 CHAR),
    AUD_ACTION    VARCHAR2(10 CHAR) not null,
    AUDIT_DATE    TIMESTAMP(6)      not null,
    AUDIT_USER    VARCHAR2(64 CHAR)
);

create table INV_APPLICATION
(
    APP_APPLICATION_ID NUMBER(19)        not null,
    CODE               VARCHAR2(10 CHAR) not null,
    PREFIX             VARCHAR2(3 CHAR)  not null,
    NAME               VARCHAR2(255 CHAR),
    CATEGORY_ID        NUMBER(19),
    SYSTEM_TYPE_ID     NUMBER(19),
    FIELD_ID           NUMBER(19),
    ADM_UNIT_ID        NUMBER(19),
    COMMISSION_ID      NUMBER(19),
    STATUS_ID          NUMBER(19)        not null,
    DESCRIPTION        CLOB,
    CREATED_AT         TIMESTAMP(6)      not null,
    CREATED_BY         VARCHAR2(64 CHAR) not null,
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(64 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR),
    EXPIRATION_DATE    TIMESTAMP(6)
);

comment on column INV_APPLICATION.APP_APPLICATION_ID is 'Unique identifier for the inventory application';
comment on column INV_APPLICATION.CODE is 'Functional corporate code of the application';
comment on column INV_APPLICATION.PREFIX is 'Three-letter architectural prefix string';
comment on column INV_APPLICATION.NAME is 'Full name descriptive of the application';
comment on column INV_APPLICATION.CATEGORY_ID is 'Foreign Key referencing the master category entity';
comment on column INV_APPLICATION.SYSTEM_TYPE_ID is 'Foreign Key referencing the master system type entity';
comment on column INV_APPLICATION.FIELD_ID is 'Foreign Key referencing the master sector field entity';
comment on column INV_APPLICATION.ADM_UNIT_ID is 'Foreign Key referencing the master administrative unit entity';
comment on column INV_APPLICATION.COMMISSION_ID is 'Foreign Key referencing the master commission entity';
comment on column INV_APPLICATION.STATUS_ID is 'Foreign Key referencing the master execution lifecycle status';
comment on column INV_APPLICATION.DESCRIPTION is 'Detailed extended functional log text context';
comment on column INV_APPLICATION.CREATED_AT is 'Timestamp tracking row baseline initialization';
comment on column INV_APPLICATION.CREATED_BY is 'User capturing row insertion context';
comment on column INV_APPLICATION.UPDATED_AT is 'Timestamp tracking last structural row modification';
comment on column INV_APPLICATION.UPDATED_BY is 'User signature metadata tracking updates';
comment on column INV_APPLICATION.DELETED_AT is 'Timestamp tracking logical deletion/deactivation milestone';
comment on column INV_APPLICATION.DELETED_BY is 'User signature capturing the logical delete action';
comment on column INV_APPLICATION.EXPIRATION_DATE is 'Validity limit deadline timestamp milestone';

create table INV_APPLICATION_AUD
(
    AUDIT_ID           NUMBER(19)        not null,
    APP_APPLICATION_ID NUMBER(19)        not null,
    CODE               VARCHAR2(10 CHAR) not null,
    PREFIX             VARCHAR2(3 CHAR)  not null,
    NAME               VARCHAR2(255 CHAR),
    CATEGORY_ID        NUMBER(19),
    SYSTEM_TYPE_ID     NUMBER(19),
    FIELD_ID           NUMBER(19),
    ADM_UNIT_ID        NUMBER(19),
    COMMISSION_ID      NUMBER(19),
    STATUS_ID          NUMBER(19)        not null,
    DESCRIPTION        CLOB,
    CREATED_AT         TIMESTAMP(6),
    CREATED_BY         VARCHAR2(64 CHAR),
    UPDATED_AT         TIMESTAMP(6),
    UPDATED_BY         VARCHAR2(64 CHAR),
    DELETED_AT         TIMESTAMP(6),
    DELETED_BY         VARCHAR2(64 CHAR),
    EXPIRATION_DATE    TIMESTAMP(6),
    AUD_ACTION         VARCHAR2(10 CHAR) not null,
    AUDIT_DATE         TIMESTAMP(6)      not null,
    AUDIT_USER         VARCHAR2(64 CHAR)
);