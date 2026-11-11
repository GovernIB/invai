### Desarrollo

#### Objetivo

Mantener los roles de proveedor, las tecnologías y las capas que se utilizan en la configuración de desarrollo de las aplicaciones. Cada tecnología pertenece a una capa.

#### Cómo se accede

Abra **Mantenimientos** → **Desarrollo**. Pulse el título de un apartado para desplegarlo; con el teclado, utilice <kbd>Tab</kbd> e <kbd>Intro</kbd>.

![Apartados del mantenimiento de Desarrollo](imgs/development.webp)

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

##### Roles de proveedor

- **Nombre**
- **Nombre en castellano**
- **Estado**

##### Tecnologías

- **Nombre**
- **Capa**
- **Estado**

##### Capas

- **Nombre**
- **Estado**

#### Tabla de resultados

Las cabeceras ordenables cambian el orden; los controles de paginación permiten recorrer la lista. Las columnas disponibles son:

##### Roles de proveedor

- **Nombre**
- **Nombre en castellano**
- **Estado**

##### Tecnologías

- **Nombre**
- **Capa**
- **Estado**

##### Capas

- **Nombre**
- **Estado**

La columna de estado se muestra por defecto cuando consulta todos los estados y se oculta cuando aplica uno.

#### Formulario

En consulta, los datos se pueden leer. Para modificarlos, active **Editar**.

##### Roles de proveedor

Obligatorios:

- **Nombre**: máximo 100 caracteres.

Opcionales:

- **Nombre en castellano**

El nombre en castellano admite hasta 100 caracteres.

![Formulario de alta de Roles de proveedor](imgs/development-provider-roles.webp)

##### Tecnologías

Obligatorios:

- **Nombre**: máximo 100 caracteres.
- **Capa**.

![Formulario de alta de Tecnologías](imgs/development-technologies.webp)

##### Capas

Obligatorios:

- **Nombre**: máximo 100 caracteres.

![Formulario de alta de Capas](imgs/development-layers.webp)

#### Guardar

**Añadir** crea el registro; **Guardar** guarda una edición. Corrija los campos señalados si hay errores. En consulta, **Aceptar** cierra el formulario. **Cancelar** abandona el alta o recupera la consulta al cancelar una edición. Si cierra una edición modificada, puede guardar o descartar los cambios.

#### Condiciones y permisos

Las operaciones dependen de los permisos de la sesión y del estado del registro. Las opciones de los desplegables proceden de los catálogos; si falta una opción necesaria, revise su mantenimiento. Un error al guardar no confirma la operación: revise el mensaje y vuelva a intentarlo cuando se haya resuelto.
