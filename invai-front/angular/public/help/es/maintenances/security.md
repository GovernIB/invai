### Seguridad

#### Objetivo

Mantener los contextos web, proveedores de identidad, tratamientos de datos personales, tipos de medida de seguridad y requisitos ENS disponibles para describir la seguridad de las aplicaciones.

#### Cómo se accede

Abra **Mantenimientos** → **Seguridad**. Pulse el título de un apartado para desplegarlo; con el teclado, utilice <kbd>Tab</kbd> e <kbd>Intro</kbd>.

![Apartados del mantenimiento de Seguridad](imgs/security.webp)

#### Acciones disponibles

- **+** abre el alta del catálogo desplegado.
- El embudo muestra u oculta los filtros.
- El selector de columnas permite elegir las columnas visibles.
- La cruz de la búsqueda rápida limpia el texto.
- El menú de tres puntos de una fila permite consultar, editar o dar de baja; la baja pide confirmación.
- Doble clic o <kbd>Intro</kbd> sobre una fila abre la consulta.
- En un registro inactivo, la restauración permite recuperarlo.
- El botón de ayuda del formulario y <kbd>Ctrl</kbd> + <kbd>K</kbd> abren este tema.

#### Búsqueda y filtrado

La búsqueda rápida filtra los registros. Para combinar criterios, abra el embudo y pulse **Buscar**. **Limpiar** restablece los filtros. **Estado** parte de **Activo**; puede seleccionar **Inactivo** o limpiarlo para consultar todos los estados.

##### Contextos web

- **Nombre**
- **Nombre en castellano**
- **Estado**

##### Proveedores de identidad

- **Nombre**
- **Estado**

##### Tratamientos de datos personales

- **Nombre**
- **Nombre en castellano**
- **Estado**

##### Tipos de medida de seguridad

- **Nombre**
- **Nombre en castellano**
- **Estado**

##### Requisitos ENS

- **Nombre**
- **Nombre en castellano**
- **Estado**

#### Tabla de resultados

Las cabeceras ordenables cambian el orden; los controles de paginación permiten recorrer la lista. Las columnas disponibles son:

##### Contextos web

- **Nombre**
- **Nombre en castellano**
- **Estado**

##### Proveedores de identidad

- **Nombre**
- **Estado**

##### Tratamientos de datos personales

- **Nombre**
- **Nombre en castellano**
- **Estado**

##### Tipos de medida de seguridad

- **Nombre**
- **Nombre en castellano**
- **Estado**

##### Requisitos ENS

- **Nombre**
- **Nombre en castellano**
- **Estado**

La columna de estado se muestra por defecto cuando consulta todos los estados y se oculta cuando aplica uno.

#### Formulario

En consulta, los datos se pueden leer. Para modificarlos, active **Editar**.

##### Contextos web

Obligatorios:

- **Nombre**: máximo 150 caracteres.
- **Nombre en castellano**: máximo 100 caracteres.

![Formulario de alta de Contextos web](imgs/security-web-contexts.webp)

##### Proveedores de identidad

Obligatorios:

- **Nombre**: máximo 150 caracteres.

![Formulario de alta de Proveedores de identidad](imgs/security-identity-providers.webp)

##### Tratamientos de datos personales

Obligatorios:

- **Nombre**: máximo 150 caracteres.
- **Nombre en castellano**: máximo 100 caracteres.

![Formulario de alta de Tratamientos de datos personales](imgs/security-personal-data-processing.webp)

##### Tipos de medida de seguridad

Obligatorios:

- **Nombre**: máximo 150 caracteres.
- **Nombre en castellano**: máximo 100 caracteres.

![Formulario de alta de Tipos de medida de seguridad](imgs/security-security-measure-types.webp)

##### Requisitos ENS

Obligatorios:

- **Nombre**: máximo 150 caracteres.
- **Nombre en castellano**: máximo 100 caracteres.

![Formulario de alta de Requisitos ENS](imgs/security-ens-requirements.webp)

#### Guardar

**Añadir** crea el registro; **Guardar** guarda una edición. Corrija los campos señalados si hay errores. En consulta, **Aceptar** cierra el formulario. **Cancelar** abandona el alta o recupera la consulta al cancelar una edición. Si cierra una edición modificada, puede guardar o descartar los cambios.

#### Condiciones y permisos

Las operaciones dependen de los permisos de la sesión y del estado del registro. Las opciones de los desplegables proceden de los catálogos; si falta una opción necesaria, revise su mantenimiento. Un error al guardar no confirma la operación: revise el mensaje y vuelva a intentarlo cuando se haya resuelto.
