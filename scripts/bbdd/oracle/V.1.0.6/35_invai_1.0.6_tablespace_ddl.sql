-- Tablespace of the LOB columns (INV_APP_DOC_ATTACHMENT_VERSION.CONTENT): the database standard asks for LOBs in their own tablespace.
-- Adapt the DATAFILE path to the environment. It needs a user allowed to create tablespaces and to alter users.
CREATE TABLESPACE INVAI_LOB
    DATAFILE '/opt/oracle/oradata/invai_lob.dbf'
    SIZE 100M
    AUTOEXTEND ON NEXT 10M MAXSIZE UNLIMITED;

ALTER USER INVAI QUOTA UNLIMITED ON INVAI_LOB;
