-- 1. Eliminar secuencias
BEGIN
    FOR seq IN (SELECT sequence_name FROM user_sequences)
        LOOP
            EXECUTE IMMEDIATE 'DROP SEQUENCE ' || seq.sequence_name;
        END LOOP;
END;
/


-- 2. Eliminar tablas con CASCADE CONSTRAINTS
BEGIN
    FOR tbl IN (SELECT table_name FROM user_tables)
        LOOP
            EXECUTE IMMEDIATE 'DROP TABLE ' || tbl.table_name || ' CASCADE CONSTRAINTS';
        END LOOP;
END;
/

BEGIN
    FOR idx IN (SELECT index_name FROM user_indexes WHERE index_type = 'NORMAL')
        LOOP
            EXECUTE IMMEDIATE 'DROP INDEX ' || idx.index_name;
        END LOOP;
END;
/
