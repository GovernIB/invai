# Scripts de Inicialización Completa de Base de Datos (`full-scripts-init`)

Esta carpeta contiene el conjunto de scripts SQL necesarios para la creación e inicialización completa de la base de
datos SIVAP en Oracle. Los scripts aquí presentes permiten desplegar una base de datos desde cero, incluyendo toda la
estructura y datos mínimos necesarios para el funcionamiento de la aplicación.

## ¿Qué incluye este conjunto de scripts?

- **00_invai_1.0.1_drop_database_pl.sql**: Elimina los objetos existentes para garantizar una instalación limpia (opcional, usar con
  precaución).
- **01_invai_1.0.1_create_sequence_ddl.sql**: Crea todas las secuencias necesarias para la generación de claves primarias y otros usos.
- **02_invai_1.0.1_create_table_ddl.sql**: Crea todas las tablas de la base de datos SIVAP.
- **03_invai_1.0.1_create_index_ddl.sql**: Crea los índices requeridos para optimizar el rendimiento de las consultas.
- **04_invai_1.0.1_create_constraints_ddl.sql**: Define todas las restricciones (PRIMARY KEY, FOREIGN KEY, UNIQUE, CHECK, etc.).
- **05_invai_1.0.1_grants_ddl.sql**: (Si existe) Concede los permisos necesarios a los usuarios de la base de datos.
- **06_.._XX_dml_sentences.sql**: Scripts de inserción de datos mínimos y de configuración inicial (valores de
  catálogos, parámetros, etc.).

## Propósito

Estos scripts permiten:

- Inicializar una base de datos completamente funcional para entornos de desarrollo, pruebas o producción.
- Garantizar que la base de datos resultante contiene **todos los cambios y evoluciones** aplicados hasta la versión
  **1.3.6** incluida.
- Facilitar la creación de entornos limpios y reproducibles.

## Consideraciones

- **No utilizar estos scripts sobre una base de datos en producción con datos existentes**, ya que eliminan y recrean
  todos los objetos.
- El orden de ejecución de los scripts es importante y está numerado para evitar errores de dependencias.
- Si necesitas aplicar solo cambios incrementales, utiliza los scripts de la carpeta de versiones (`V.x.x.x`).

## Uso recomendado

1. Asegúrate de tener una base de datos Oracle vacía o que pueda ser sobrescrita.
2. Ejecuta los scripts en el orden numérico ascendente.
3. Verifica que no existan errores en la ejecución y revisa los logs generados.

## Alcance de versión

- **Incluye todos los cambios y evoluciones hasta la versión 1.3.6**.
- Las carpetas `V.x.x.x` pueden seguir utilizándose como histórico de cambios incrementales.

---

Para más información sobre la automatización y ejecución de scripts, consulta el README principal de la carpeta `bbdd`.

