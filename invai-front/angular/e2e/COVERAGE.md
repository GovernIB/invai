# Cobertura e2e

Auditoría del 14/10/2026 (rama `sprint/sprint6`) y seguimiento del plan para recuperarla.
La suite se ejecuta solo en local (`npm run test:e2e`), contra el backend local y con login real
en el IdP; no forma parte del pipeline de CI.

Leyenda: `[x]` hecho · `[ ]` pendiente · **R** = backend real · **M** = `page.route` (mock).

## Cómo ejecutar

- Suite completa: `npm run test:e2e` (hace login con `E2E_USERNAME`/`E2E_PASSWORD` de `.env`).
- Un fichero: `npx playwright test e2e/<fichero>.spec.ts --project=chromium --no-deps`.
- Usa siempre `--no-deps` si hay otra ejecución en marcha: el backend admite **una sola sesión por
  usuario** y el proyecto `setup` vuelve a hacer login, lo que expira la sesión de la otra
  ejecución (respuestas "This session has been expired...").
- El backend solo **lee** de Soffid (usuarios, roles y grupos); los tests con backend real necesitan
  la VPN para que esas lecturas respondan, pero ningún test escribe en Soffid.

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

### Aplicaciones (M)

- [x] Seguretat: roles (`application-security-roles.spec.ts`) y ENS, riesgos y medidas
  (`application-security-ens.spec.ts`)
- [x] Roles Soffid tras crear, cambiar prefijo y restaurar (`application-soffid-roles.spec.ts`)
- [x] Validación DIR3 masiva de la lista (`applications-dir3-audit.spec.ts`)
- [x] Integracions: `rolesUnavailable`, edición, conexión a aplicación, validaciones, alta del
  registro (`application-integrations-connections.spec.ts`)
- [x] Documentació: adjuntos contra un backend simulado de `application/documentation` y sus
  catálogos (búsqueda por título, filtros por sección, estado, tipo de documento, categoría y
  subcategoría, orden, alta multipart con categoría activa, rechazo de
  ficheros con formato no admitido y bloqueo del adjunto sin catálogo de formatos, consulta, nueva
  versión, descarga y visualización, edición, eliminación) y enlaces contra un backend simulado
  de `application/documentation/link`; aplicación inactiva (`application-documentation.spec.ts`)
- [x] Dades: alta del registro, validación de URL, observación, comprobación de cada URL con
  `application/data/check-url` (`application-data-validation.spec.ts`)
- [x] Sistemas/BD: catálogos con búsqueda y filtros (`application-systems-databases-catalogs.spec.ts`)
- [x] Responsables: bajas con observación, edición, autorizados, DIR3 de la aplicación
  (`application-responsibles.spec.ts`)
- [x] Accessibilitat (`application-accessibility.spec.ts`)

### Mantenimientos

- [ ] Responsables · Personal extern, Empreses, Autoritzacions (R + M)
- [ ] Transferencia de roles: revocar, descartar el lote, destino solo en Soffid, error al aplicar (M)
- [x] Seguretat (5 recursos) y Accessibilitat (2 recursos) (R) — `maintenance-security.spec.ts`,
  `maintenance-accessibility.spec.ts`
- [x] Sistemes externs: consultar, editar, filtro de empresa, restaurar (R) —
  `external-systems-lifecycle.spec.ts`
- [x] General: restaurar entorno, filtros de comisiones, estado no soportado —
  `maintenance-general-filters.spec.ts`
- [x] Diálogo CRUD compartido: cambios sin guardar, validaciones, Neteja — `crud-dialog.spec.ts`
- [x] Documentació · categorías documentales: subcategorías desplegables, altas, mover una
  subcategoría, eliminar y restaurar, búsqueda y 320 px (M) — `document-categories.spec.ts`
- [x] Documentació · detalle de una categoría: consulta, guardar nombres y permisos (simulados),
  aviso de cambios sin guardar, editar una subcategoría, categoría inactiva (M) —
  `document-categories.spec.ts`

### Shell y transversal

- [x] Login sin sesión, logout, tamaño de letra, ayuda, idioma, sidebar, diálogo de error 400 y
  recuperación tras 401 (`shell.spec.ts`)
- [x] Escaneo axe WCAG 2.2 AA de la lista, las 9 pestañas del detalle y las 8 de mantenimientos
  (`accessibility-scan.spec.ts`, con `@axe-core/playwright`)
  más las reglas `landmark-*` (best-practice) para que los `nav`/`aside` del layout tengan
  nombres accesibles únicos

## 3. Bugs de la app detectados (marcados con `test.fixme` o como infracción conocida)

- [ ] Seguretat: tras "Validar manualment" el foco acaba en `<body>` en lugar de la fila del rol.
- [ ] Sistemas/BD: los diálogos de relación nunca muestran "Selecciona un servidor/una base de dades"
  (`isInvalid` es un `computed()` sobre el estado del formulario, que no es una signal).
- [ ] Comisiones: la fecha en modo consulta usa el locale del navegador (`new Intl.DateTimeFormat()`).
- [ ] Ayuda: el diálogo no tiene nombre accesible (cabecera con plantilla propia).
- [ ] Desarrollo: el enlace deshabilitado al código fuente es un `<span>` con `aria-label` (axe
  `aria-prohibited-attr`).
- [ ] Categories, Sistemes d'informació y Àmbits: al elegir "Inactiu" el desplegable lo sigue
  mostrando pero la búsqueda vuelve a "Actiu".
- [ ] Proveedores de BD: `aria-invalid`/`aria-describedby` del puerto están en el contenedor, no en
  el input; y tras cerrar un aviso de cambios sin guardar el foco va a `<body>`.
- [ ] Integración con el backend 33a75c66: la lista ya no recibe `status` ni `commission`, así que
  las columnas Estat y Comissió quedan vacías y "Edita" no se bloquea en filas inactivas.
