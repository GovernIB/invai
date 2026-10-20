
SET SERVEROUTPUT ON SIZE UNLIMITED
WHENEVER SQLERROR EXIT FAILURE

DECLARE
    TYPE t_fk IS RECORD (
        table_name      user_constraints.table_name%TYPE,
        constraint_name user_constraints.constraint_name%TYPE
    );
    TYPE t_fk_list IS TABLE OF t_fk;

    v_fks         t_fk_list := t_fk_list();
    v_fk_count    PLS_INTEGER := 0;
    v_tables      PLS_INTEGER := 0;
    v_seqs        PLS_INTEGER := 0;
    v_restart_ok  BOOLEAN := TRUE;

    PROCEDURE enable_fks IS
    BEGIN
        FOR i IN 1 .. v_fks.COUNT LOOP
            EXECUTE IMMEDIATE 'ALTER TABLE "' || v_fks(i).table_name
                              || '" ENABLE CONSTRAINT "' || v_fks(i).constraint_name || '"';
        END LOOP;
        v_fks.DELETE;
    END enable_fks;
BEGIN
    -- 1) Deshabilitar todas las foreign keys habilitadas
    FOR c IN (SELECT table_name, constraint_name
              FROM user_constraints
              WHERE constraint_type = 'R'
                AND status = 'ENABLED'
              ORDER BY table_name, constraint_name)
    LOOP
        EXECUTE IMMEDIATE 'ALTER TABLE "' || c.table_name
                          || '" DISABLE CONSTRAINT "' || c.constraint_name || '"';
        v_fks.EXTEND;
        v_fks(v_fks.LAST).table_name      := c.table_name;
        v_fks(v_fks.LAST).constraint_name := c.constraint_name;
        v_fk_count := v_fk_count + 1;
    END LOOP;

    -- 2) Truncar todas las tablas (si falla, se rehabilitan las FK antes de propagar el error)
    BEGIN
        FOR t IN (SELECT table_name
                  FROM user_tables
                  WHERE temporary = 'N'
                    AND nested = 'NO'
                    AND dropped = 'NO'
                    AND iot_name IS NULL
                  ORDER BY table_name)
        LOOP
            EXECUTE IMMEDIATE 'TRUNCATE TABLE "' || t.table_name || '"';
            v_tables := v_tables + 1;
        END LOOP;
    EXCEPTION
        WHEN OTHERS THEN
            enable_fks;
            RAISE;
    END;

    -- 3) Rehabilitar las foreign keys
    enable_fks;

    -- 4) Reiniciar todas las secuencias
    FOR s IN (SELECT sequence_name FROM user_sequences ORDER BY sequence_name)
    LOOP
        BEGIN
            EXECUTE IMMEDIATE 'ALTER SEQUENCE "' || s.sequence_name || '" RESTART';
            v_seqs := v_seqs + 1;
        EXCEPTION
            WHEN OTHERS THEN
                v_restart_ok := FALSE;
                EXIT;
        END;
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('Foreign keys deshabilitadas y rehabilitadas: ' || v_fk_count);
    DBMS_OUTPUT.PUT_LINE('Tablas vaciadas: ' || v_tables);
    IF v_restart_ok THEN
        DBMS_OUTPUT.PUT_LINE('Secuencias reiniciadas: ' || v_seqs);
    ELSE
        DBMS_OUTPUT.PUT_LINE('AVISO: RESTART de secuencias no disponible (Oracle anterior a 18c); secuencias sin reiniciar.');
    END IF;
END;
/
