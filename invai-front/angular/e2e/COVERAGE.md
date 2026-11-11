# Cobertura e2e

Auditoría del 14/10/2026 (rama `sprint/sprint6`) y seguimiento del plan para recuperarla.
La suite se ejecuta solo en local (`npm run test:e2e`), contra el backend local y con login real
en el IdP; no forma parte del pipeline de CI.

Desde el 23/10/2026 **todos los specs** hacen login real en el IdP y usan el backend real: si el
login falla, el test falla con el texto que mostraba el IdP. Cada test crea sus datos por API
(aplicación desechable, categorías, documentos, conexiones, personas...) y los da de baja al
terminar. `page.route` solo se usa para:

- responder con un error la petición concreta que la prueba quiere ver fallar (`failApiRequest`
  de `support/http.ts`: 400, 401, 403, 500, catálogos que no cargan...);
- dos estados de error del backend que no se pueden provocar: Soffid caído al listar conexiones
  (`rolesUnavailable`) y dos clasificaciones ENS activas; se modifica la respuesta real;
- retener una petición real (`assign-roles`, comprobación DIR3) para ver el estado intermedio;
- el bundle `es` de la build localizada, que `ng serve` no sirve (`shell.spec.ts`).

Leyenda: `[x]` hecho · `[ ]` pendiente.

## Cómo ejecutar

- Suite completa: `npm run test:e2e` (hace login con `E2E_USERNAME`/`E2E_PASSWORD` de `.env`).
- Un fichero: `npx playwright test e2e/<fichero>.spec.ts --project=chromium --no-deps`.
- Usa siempre `--no-deps` si hay otra ejecución en marcha: el backend admite **una sola sesión por
  usuario** y el proyecto `setup` vuelve a hacer login, lo que expira la sesión de la otra
  ejecución (respuestas "This session has been expired...").
- El backend solo **lee** de Soffid (usuarios, roles y grupos); los tests necesitan la VPN para que
  esas lecturas respondan, pero ningún test escribe en Soffid. Sin VPN el backend responde "El
  servei de cerca de personal de Soffid no està disponible" y fallan los specs de seguretat,
  integracions, responsables, DIR3 y roles de Soffid.
- `applications-dir3-audit.spec.ts` lanza la validación DIR3 real de **todas** las aplicaciones
  activas, que guarda su resultado.

## Datos reales de los que dependen

Si cambian, los tests afectados fallan; los valores están en los helpers de `support/`.

- Soffid: el usuari `u08887` (Maria Lourdes Batanero) tiene exactamente los rols `usuari-tipus-E`
  y `LLU_CENTRES` y no `INV_ADMIN` (`application-integrations.ts`); Francisco Planas (`u80657`)
  tiene el DIR3 `A04027006` y Maria Elvira Bibiloni (`u02173`) no tiene DIR3
  (`application-responsibles.ts`); los rols `LLU_CENTRES`, `LLU_CONSELLERIA`, `ICS_INVIT` e
  `INV_ADMIN` (`application-security.ts`).
- Prefijos libres con roles de Soffid (`application-prefix.ts`): `LLU`, `OWS`, `PID`, `ICS` (varios
  roles) e `ISI`, `FAC`, `INT`... (un rol). Se toman prestados y la limpieza devuelve a la
  aplicación su prefijo antes de darla de baja, porque un prefijo queda ocupado aunque la
  aplicación esté inactiva.
- `https://www.caib.es/` responde a `application/data/check-url`.
- Catálogos de referencia: niveles de seguridad, proveedores de identidad, segmentos, situaciones
  de cumplimiento, tipos de autorización, secciones, estados y formatos de documento, la aplicación
  INVAI y el sistema externo Soffid.

## Casos que el backend real no puede producir

Se quitaron del e2e; los cubren los tests unitarios indicados en cada spec.

- Usuario sin nombre completo (`user-menu.component.spec.ts`).
- Aplicación sin registro de Dades, Integracions, Seguretat o Accessibilitat: se crean con la
  aplicación y uno dado de baja sigue enlazado.
- Nombre de un context web de más de 150 caracteres: el backend también lo limita.
- Rol `NOT_VALIDATED` y su "Validar manualment": `recalculateValidation` no tiene ningún llamador.
- Personas de Soffid sin código de usuario.
- Combinaciones de discrepancias y aplicaciones no validadas al acabar la auditoría DIR3.

## Convenciones de locators

- Las pestañas del detalle añaden un texto oculto cuando la sección está incompleta: usar
  `/^Responsables/`, nunca el nombre exacto.
- Los botones de filtros añaden "(N filtres aplicats)" a su nombre accesible.
- En modo consulta los campos son `readonly` (`not.toBeEditable()`) o texto estático
  `<p aria-labelledby="<id>-label">`; nunca `disabled`.
- La lista, POST, PUT y reactivate de aplicación devuelven el DTO reducido (sin `status` ni
  `description`); el detalle (`GET /application/{id}`) trae el completo.

## 1. Tests desfasados reparados

- [x] `applications.spec.ts` — DTO reducido (backend 33a75c66) y `readonly` en General
- [x] `application-security.spec.ts` — sustituido por `application-security-roles.spec.ts`
- [x] `application-web-contexts.spec.ts` — "Verificat" es un icono; pestaña por regex
- [x] `application-development.spec.ts` — `readonly`; observación estática fuera de edición
- [x] `application-systems-databases.spec.ts` — pestaña por regex, botón de filtros, observación estática
- [x] `application-integrations.spec.ts` — sin columna de coincidencia; icono de discrepancia
- [x] `maintenances.spec.ts` — 8 pestañas, paneles reordenados, transferencia con `/person/all` y
  `toPersonEmailAddress`, vistas `readonly`/estáticas, tabla ancha a 800 px
- [x] `systems.spec.ts` y `development-maintenances.spec.ts` — selecciones como texto estático

## 2. Cobertura nueva

### Aplicaciones

- [x] Seguretat: roles (`application-security-roles.spec.ts`) y ENS, riesgos y medidas
  (`application-security-ens.spec.ts`)
- [x] Roles Soffid tras crear, cambiar prefijo y restaurar (`application-soffid-roles.spec.ts`)
- [x] Validación DIR3 masiva de la lista (`applications-dir3-audit.spec.ts`)
- [x] Integracions: `rolesUnavailable`, edición, conexión a aplicación, validaciones, alta del
  registro (`application-integrations-connections.spec.ts`)
- [x] Documentació: adjuntos (búsqueda por título, filtros por sección, estado, tipo de documento,
  categoría y subcategoría, orden, alta multipart con categoría activa, rechazo de ficheros con
  formato no admitido y bloqueo del adjunto sin catálogo de formatos, consulta, nueva versión,
  descarga y visualización, edición, eliminación) y enlaces; aplicación inactiva
  (`application-documentation.spec.ts`)
- [x] Dades: alta del registro, validación de URL, observación, comprobación de cada URL con
  `application/data/check-url` (`application-data-validation.spec.ts`)
- [x] Sistemas/BD: catálogos con búsqueda y filtros (`application-systems-databases-catalogs.spec.ts`)
- [x] Responsables: bajas con observación, edición, autorizados, DIR3 de la aplicación
  (`application-responsibles.spec.ts`)
- [x] Accessibilitat (`application-accessibility.spec.ts`)

### Mantenimientos

- [ ] Responsables · Personal extern, Empreses, Autoritzacions
- [x] Transferencia de roles entre dos personas del test (`maintenances.spec.ts`)
- [ ] Transferencia de roles: revocar, descartar el lote, destino solo en Soffid, error al aplicar
- [x] Seguretat (5 recursos) y Accessibilitat (2 recursos) — `maintenance-security.spec.ts`,
  `maintenance-accessibility.spec.ts`
- [x] Sistemes externs: consultar, editar, filtro de empresa, restaurar —
  `external-systems-lifecycle.spec.ts`
- [x] General: restaurar entorno, filtros de comisiones, estado no soportado —
  `maintenance-general-filters.spec.ts`
- [x] Diálogo CRUD compartido: cambios sin guardar, validaciones, Neteja — `crud-dialog.spec.ts`
- [x] Documentació · categorías documentales: subcategorías desplegables, altas, mover una
  subcategoría, eliminar y restaurar, búsqueda y 320 px — `document-categories.spec.ts`
- [x] Documentació · detalle de una categoría: consulta, guardar nombres y permisos (en memoria del front, el backend aún no los guarda),
  aviso de cambios sin guardar, editar una subcategoría, categoría inactiva —
  `document-categories.spec.ts`

### Shell y transversal

- [x] Login sin sesión, logout, tamaño de letra, idioma, sidebar, diálogo de error 400 y
  recuperación tras 401 (`shell.spec.ts`)
- [x] Centro de ayuda (`help-center.spec.ts`): abrir desde el menú de usuario y con Ctrl + K,
  nombre del diálogo, foco inicial en el buscador y vuelta del foco al cerrar con Escape, abrir en
  el tema de la pantalla (lista y alta de aplicaciones) con su descripción «Tema obert: …», abrir un
  tema, marcarlo como actual (`aria-current`) y llevar el foco a su encabezado, plegar y desplegar
  un grupo del índice con el teclado, buscar sin acentos y abrir el resultado con el teclado,
  el primer Escape cierra solo las sugerencias, y el aviso y el reintento cuando no cargan el
  índice o un tema (`failApiRequest` sobre el archivo estático). Los estados de carga y de índice
  vacío los cubren los tests unitarios (`help-modal.spec.ts`).
  En la ficha se comprueba el tema propio de las nueve pestañas, sus imágenes y el foco en el buscador.
  `help-screenshots/application-detail-technical.spec.ts` comprueba los tres temas de la 5.3 en
  catalán y castellano: imagen cargada, foco, Axe a 320 px y reflujo con texto al 200%.
  `help-screenshots/application-detail-information.spec.ts` aplica las mismas comprobaciones a Datos,
  Integraciones, Documentación y la ficha propia de un documento, con datos de ejemplo y sin enviar
  el correo cuya ventana se captura.
- [x] Botones contextuales y scroll (`help-context-buttons.spec.ts`): apertura desde cabecera
  y formulario, tema correspondiente, Escape conserva el formulario y devuelve el foco,
  scroll independiente del índice y artículo con buscador fijo, reflujo móvil y Axe de la ayuda.
- [x] Escaneo axe WCAG 2.2 AA de la lista, las 9 pestañas del detalle, las 8 de mantenimientos y el
  centro de ayuda abierto en el tema de la pantalla, con otro tema elegido, con los temas de
  «Primers passos», los de Desarrollo, Accesibilidad y Seguridad y con un grupo plegado
  (`accessibility-scan.spec.ts`, con
  `@axe-core/playwright`)
  más las reglas `landmark-*` (best-practice) para que los `nav`/`aside` del layout tengan
  nombres accesibles únicos; y el contraste de la cabecera del primer panel de cada pestaña de mantenimiento con el
  puntero encima (antes dependía de dónde quedaba el ratón: el gris de PrimeNG bajaba a 4,34:1 y
  se corrigió en `maintenance-accordion.scss`)

## 3. Bugs de la app detectados (marcados con `test.fixme` o como infracción conocida)

- [ ] Diálogos CRUD (visto al documentar el correo en la ayuda 5.4): el diálogo de «Envia per correu»
  tiene un encabezado visible, pero carece de nombre accesible. La cabecera de `crud-entity-dialog`
  no enlaza su `h2` con el `aria-labelledby` del `p-dialog`. Se localiza por su encabezado en la
  captura; corregir el contrato de nombres del componente compartido en una tarea propia.
- [ ] Ficha de documento (visto en las capturas de ayuda 5.4): en castellano, la categoría y la
  subcategoría pueden aparecer con su nombre catalán, aunque se hayan creado también con `nameEs`;
  la lista de adjuntos sí usa el nombre castellano. Revisar los datos y el fallback de las opciones
  de la ficha. Las capturas sustituyen ambos nombres por sus etiquetas de ejemplo.
- [ ] Seguretat: tras "Validar manualment" el foco acaba en `<body>` en lugar de la fila del rol
  (solo reproducible con roles `NOT_VALIDATED` heredados; cubierto por tests unitarios).
- [ ] Sistemas/BD: los diálogos de relación nunca muestran "Selecciona un servidor/una base de dades"
  (`isInvalid` es un `computed()` sobre el estado del formulario, que no es una signal).
- [ ] Comisiones: la fecha en modo consulta usa el locale del navegador (`new Intl.DateTimeFormat()`).
- [x] Ayuda: el diálogo no tenía nombre accesible (cabecera con plantilla propia). Corregido el
  05/11/2026: el título enlaza el `id` que PrimeNG pasa a la plantilla.
- [x] Ayuda: el buscador borraba el texto escrito al cerrarse las sugerencias (`forceSelection`).
  Corregido el 05/11/2026.
- [x] Ayuda: al desplegar un grupo de temas, el PanelMenu de PrimeNG pintaba un `<ul>` cuyo único
  hijo era otro `<ul>` (axe `list`). Corregido el 05/11/2026 con un índice nativo (D-12 en
  `docs/help-center/PLAN.md`); el `p-tree` se descartó porque anuncia mal la posición de cada tema.
  El escaneo de la ayuda pasa sin infracciones.
- [ ] Desarrollo: el enlace deshabilitado al código fuente es un `<span>` con `aria-label` (axe
  `aria-prohibited-attr`).
- [ ] Categories, Sistemes d'informació y Àmbits: al elegir "Inactiu" el desplegable lo sigue
  mostrando pero la búsqueda vuelve a "Actiu".
- [ ] Proveedores de BD: `aria-invalid`/`aria-describedby` del puerto están en el contenedor, no en
  el input; y tras cerrar un aviso de cambios sin guardar el foco va a `<body>`.
- [ ] Integración con el backend 33a75c66: la lista ya no recibe `status` ni `commission`, así que
  las columnas Estat y Comissió quedan vacías y "Edita" no se bloquea en filas inactivas.
- [ ] DIR3: con la comprobación real, que responde con el diálogo ya abierto, el foco se queda en la
  acción de la fila en lugar de entrar en el diálogo "Confirmar discrepància DIR3".
- [ ] DIR3: el POST de un responsable devuelve la persona con todos los campos vacíos; si falla la
  validación manual tras crearlo, el diálogo dice que falta el correo y deja "Confirmar"
  deshabilitado, así que no se puede reintentar.
- [ ] Backend: al asignar una persona de Soffid que ya está en la base de datos sin código de
  usuario (Juan Ferra, id 2), no se le guarda el código, así que la tabla no lo muestra.
- [ ] Menú lateral (visto al documentar la ayuda): «Documentació», «Ut. Orgànica» e «Informes»
  salen en catalán en la versión en castellano; en `menu-items.ts` son textos fijos, sin
  `$localize`.
- [ ] Lista de aplicaciones (visto al documentar la ayuda): el texto de la búsqueda rápida sale
  como «Cerca ràpida» en la versión en castellano.
- [ ] Idioma (visto al documentar la ayuda): al cambiar de idioma se pierde la pantalla en la que
  se estaba. `LanguagePreferenceService` solo cambia el prefijo del idioma, pero las rutas están
  traducidas (`aplicacions` / `aplicaciones`), así que la ruta no existe en el otro idioma y `**`
  lleva a la lista de aplicaciones.
- [ ] Selector de columnas de las listas (visto al documentar la ayuda): el `combobox` oculto de
  `p-multiSelect` tiene como valor «[object Object],[object Object],…», que es lo que leería un
  lector de pantalla después de «Mostra o oculta columnes».
- [ ] Responsables de la ficha (visto al documentar la ayuda): no se comprueba el permiso
  `responsibleWrite`. Un usuario que solo puede consultar ve «Editar» y los botones de añadir, y al
  confirmar una ventana recibe «No tens permisos per fer aquesta operació.» (HTTP 403).
- [ ] Sistemes i BD (visto al documentar la ayuda): la confirmación de «Elimina» dice «La relació
  amb el servidor #{id} quedarà inactiva.» con el identificador interno de la relación en lugar del
  nombre, y sus botones se anuncian con los nombres genéricos «Cancel·la l'acció» y «Confirma
  l'acció».
- [ ] General de la ficha (visto al documentar la ayuda): «Data de creació» se muestra como
  «06-11-2026» y la fecha de la comisión como «12/01/2026»; dos formatos de fecha en la misma
  pestaña.
- [x] Diálogos (visto en la fase 3b de la ayuda): el ✕ de cuatro `p-dialog` no tenía nombre
  (axe `button-name`, crítico) porque PrimeNG solo lo nombra con `closeAriaLabel`. Corregido el
  06/11/2026 en el diálogo de errores del servidor, el aviso de cambios sin desar de la ficha, el
  aviso de responsabilidades asignadas y el diálogo de confirmación.
# Ayuda 5.5: observaciones del entorno local (06/11/2026)

- Durante las capturas de Responsables, la búsqueda de Soffid devuelve «El servei de cerca de
  personal de Soffid no està disponible en aquest moment». Se captura el formulario vacío y su
  opción de reintento; no se verifica una transferencia ni una revocación real.
- El correo de Personal extern tiene un límite efectivo de 150 caracteres en el formulario,
  mientras el mensaje de longitud menciona 254. La ayuda documenta el límite efectivo.

## Ayuda 5.6: hallazgos previos revisados (06/11/2026)

- La tabla de permisos de categorías documentales conserva las casillas deshabilitadas en
  consulta (`permissionsForm.disable`), con textos Permès/Denegat junto a cada valor. Sigue
  pendiente sustituir esta presentación por valores estáticos según la skill de formularios.
- El auditor de tablas sigue señalando la ausencia de contenedor/capa de carga estándar en
  `document-category-permissions-table.html`. Existía en la línea base. Este bloque añade ayuda
  contextual y documentación; ambos hallazgos se conservan para su corrección específica.
- Las capturas usan altas vacías y una categoría de ejemplo creada y eliminada por cada prueba
  de detalle. No se modifican permisos ni catálogos existentes.
