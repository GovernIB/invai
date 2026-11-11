# Centro de ayuda · plan por fases

Seguimiento de la implantación del centro de ayuda de INVAI, empezada el 05/11/2026. El modal de
partida era una copia casi literal del de SIVAP, sin temas (`public/help/index.json` estaba vacío).

Desde el 05/11/2026 la referencia es la guía de ayuda de RUPER (`ruper-front/READMES/Ayuda.md`),
más reciente que SIVAP y del mismo tipo de aplicación. Más abajo,
[Guía de RUPER adaptada a INVAI](#guía-de-ruper-adaptada-a-invai) traduce sus nombres a los de
INVAI. Las decisiones que salen de ella (D-14 a D-25) se aceptaron el 05/11/2026.

Cada fase termina con las comprobaciones automáticas en verde y una lista de qué revisar a mano.
No se empieza la siguiente sin el visto bueno de la anterior.

## Decisiones

| Id | Decisión | Estado |
| --- | --- | --- |
| D-01 | El contenido es estático y vive en `public/help/<idioma>/` (`ca`, `es`). El idioma sale de `LOCALE_ID`; si no es `ca` ni `es` se usa `ca`, el idioma fuente. Los dos idiomas tienen el mismo árbol de temas. | Aplicada (fase 1); confirmada con D-14 |
| D-02 | El índice `index.json` es una lista de `HelpTopic` (`label`, `icon?`, `document?`, `items?`) propia de INVAI. El componente la convierte en `MenuItem` de PrimeNG; el JSON no depende de PrimeNG. RUPER usa el mismo árbol con `value` en lugar de `document`. | Aplicada (fase 1) |
| D-03 | Cada documento empieza con un encabezado `###` y sus apartados usan `####`: el título del diálogo es `h2`. En RUPER el título del diálogo no es un encabezado y los documentos empiezan con `##`. | Aplicada (fase 1) |
| D-04 | Los documentos usan `github-markdown-light.css`. INVAI no tiene modo oscuro y la hoja automática pintaba el texto casi blanco cuando el sistema operativo estaba en modo oscuro. | Aplicada (fase 1) |
| D-05 | El componente vive en `core/components/help-modal`: se monta una vez en `app.html` y no pertenece al menú de usuario, que solo lo abre. | Aplicada (fase 1) |
| D-06 | Las imágenes van en `imgs/` junto a su documento, en `.webp` y con texto alternativo. Las rutas se resuelven respecto al documento. | Aplicada (fase 1) |
| D-07 | Las capturas de pantalla se generan con Playwright contra el back-end real (necesita VPN), no a mano. El PNG de Playwright se convierte a WebP en el propio Chromium (`canvas.toDataURL('image/webp')`), sin dependencias nuevas. Las de `es` se toman con `npm run local-es`. RUPER las hace a mano con el MCP chrome-devtools y una persona que inicia sesión: el script de INVAI se puede repetir cuando cambia una pantalla. | Aplicada (fase 5.0); se amplía con D-22 |
| D-08 | Ayuda contextual como en RUPER: `Ctrl + K` abre el tema del último diálogo abierto que lo tenga, si no el de la pantalla y, si tampoco, el último consultado. Las rutas lo declaran en su `data` y los diálogos al abrirse; los temas están en una constante `HELP_TOPICS` y un test comprueba que siguen en los dos `index.json`. Se hace **antes de la 5.2**: decide si cada pantalla o diálogo necesita un tema propio. | Aplicada (fase 6); los diálogos de la ficha, cuando tengan tema |
| D-09 | La búsqueda mira solo el texto visible (sin sintaxis Markdown, rutas de imagen ni URL), sin distinguir mayúsculas ni acentos («aplicacio» encuentra «Aplicació»). Los resultados siguen el orden del menú. | Aplicada (fase 2); ampliada con D-17 |
| D-10 | La búsqueda empieza a partir de 2 caracteres: con 1 casi todos los temas coincidían. RUPER usa el mismo mínimo. | Aplicada (fase 2) |
| D-11 | Cada resultado es el título del tema más un fragmento en texto plano con la coincidencia en negrita (`<mark>`). El nombre accesible de la opción es el título del tema y, desde D-17, también su grupo. | Aplicada (fase 2) |
| D-12 | El índice de temas deja el PanelMenu de PrimeNG por una navegación nativa, porque PanelMenu anida un `<ul>` dentro de otro (axe `list`). Cada grupo es un botón con `aria-expanded` y `aria-controls` que muestra u oculta su lista; cada tema, un botón que abre su documento. En cada apertura solo se despliegan los antecesores del tema activo; durante la consulta se abre la rama de cualquier tema elegido y se conservan las expansiones manuales. El tema abierto lleva `aria-current="true"`, negrita y una barra lateral, no solo color. Se recorre con **Tab**; **Intro** y **Espai** abren el tema o pliegan el grupo. | Aplicada el 05/11/2026 (fase 3), tras descartar D-18; apertura revisada el 06/11/2026 |
| D-13 | Solo se documentan pantallas que existen: «Documentació» es una página vacía y «Ut. Orgànica» e «Informes» no tienen ruta. Se añadirán cuando existan. | Aplicada (fase 5) |
| D-14 | Se mantienen los dos idiomas. RUPER escribe la ayuda solo en castellano y cita los botones en catalán (**Desar**); en INVAI el que usa la versión en castellano ve **Guardar**, y cada nombre catalán dentro de un texto en castellano necesitaría `lang="ca"` (WCAG 3.1.2). Coste: doble texto y doble tanda de capturas. | Aceptada el 05/11/2026 |
| D-15 | Estructura del tema como RUPER, sin emojis ni separadores `---` (los lectores de pantalla leen «dardo», «lápiz», «separador»). Apartados, en este orden y solo los que apliquen: Objectiu / Objetivo · Com s'hi accedeix / Cómo se accede (en la 5.0b se igualó al catalán; antes «Acceso a la pantalla») · Accions disponibles / Acciones disponibles · Cerca i filtres / Búsqueda y filtrado · Taula de resultats / Tabla de resultados · Formulari / Formulario (un subapartado por operación, con los campos obligatorios y los opcionales en listas separadas) · Desar / Guardar · Condicions i permisos / Condiciones y permisos. La pantalla se documenta entera: todas las acciones, campos y opciones de cada menú, una por línea. | Aceptada el 05/11/2026 |
| D-16 | Los `.md` no llevan comentarios HTML ni notas internas: son ficheros estáticos que se publican tal cual, así que un `<!-- REVISAR -->` llega a producción y lo puede leer cualquiera. Lo que no se puede comprobar en la app va a [Dudas para el equipo](#dudas-para-el-equipo) y el tema solo cuenta lo comprobado. | Aplicada (5.1) |
| D-17 | Búsqueda por palabras como RUPER: un tema coincide si tiene todas las palabras, juntas o no, en el título o en el texto. Primero los que las tienen todas en el título y después los que más las repiten. Cada resultado muestra también su grupo («Aplicacions › Llista d'aplicacions»), e **Intro** abre el primero. Un documento que no carga no deja la búsqueda sin resultados (antes un solo error la rompía entera). | Aplicada (fase 2) |
| D-18 | Índice con el `p-tree` de PrimeNG, como RUPER. Descartada al probarlo: el `p-tree` de PrimeNG 21 pone en cada nodo `aria-setsize` = número de hijos en lugar de hermanos (un tema se leería «1 de 0») y su botón de desplegar no tiene nombre. El Pass Through solo llega a los nodos del primer nivel, así que no se puede corregir sin tocar el DOM. | Descartada el 05/11/2026; se aplica D-12 |
| D-19 | Enlaces como RUPER: a otro tema, con su ruta relativa y su etiqueta del índice como texto, se abre dentro de la ayuda; a un apartado del mismo tema, `#` y el título sin acentos ni signos (Angular quita los `id`, así que se busca por el texto del encabezado); externos, en una pestaña nueva y avisándolo a los lectores de pantalla; a pantallas de la app, no: se explica el camino por el menú. Antes un enlace a otro `.md` sacaba al usuario de la app. | Aplicada (fase 3) |
| D-20 | Markdown admitido: tablas, `<kbd>` y `<details>`. Las teclas van con `<kbd>` (<kbd>Ctrl</kbd> + <kbd>K</kbd>) en lugar de negrita. Sin listas de tareas, estilos en línea ni `id`, que el saneado de Angular elimina. | Aceptada el 05/11/2026 |
| D-21 | El orden y los iconos del índice son los del menú lateral (`menu-items.ts`: `PrimeIcons.TH_LARGE` → `"pi pi-th-large"`; los de Manteniments salen de `MAINTENANCE_TABS` y los de la ficha, de sus pestañas). Entre hermanos no se repite icono. | Aceptada el 05/11/2026 |
| D-22 | Capturas con presupuesto de peso como RUPER: como mucho 40 KB (bajando la calidad) y fallo por encima de 70 KB, pidiendo recortar más. Anotaciones en rojo (recuadro con holgura, puntero pequeño) cuando hay que señalar un botón. Una hoja con todas las capturas de una tanda para revisarlas de una vez. | Aceptada el 05/11/2026 |
| D-23 | Mantenimiento como RUPER: `npm run help:pending` lista los commits de `src/app` posteriores a la última revisión, agrupados por tema, según `scripts/help/revision.json` (tema → carpetas de código). Los ficheros sin tema salen aparte: pueden ser una pantalla nueva. `-- --mark` da la ayuda por revisada hasta `HEAD`. | Aceptada el 05/11/2026; pendiente (fase 4) |
| D-24 | Los fallos de la app que aparezcan al documentar se anotan en el apartado de bugs de `e2e/COVERAGE.md` (el equivalente del `Bugs_Detectados.md` de RUPER) y no se corrigen sobre la marcha. | Aceptada el 05/11/2026 |
| D-25 | «Primers passos» tendrá, como RUPER, «Capçalera i menú d'usuari» (idioma, mida de la lletra, centre d'ajuda, declaració d'accessibilitat, tancar sessió, versió) y «Trucs de la interfície» (buscador de filtres, columnes, paginació, camps obligatoris i errors en desar, desplegables amb cerca, botons de només icona, dreceres de teclat). Sin tema oscuro: INVAI no lo tiene (D-04). | Aplicada (5.0b) |

## Guía de RUPER adaptada a INVAI

Qué es cada cosa de `ruper-front/READMES/Ayuda.md` en INVAI.

| RUPER | INVAI |
| --- | --- |
| Ayuda solo en castellano, en `public/help/` | Catalán y castellano, en `public/help/ca/` y `public/help/es/` (D-01, D-14) |
| `public/help/index.json`, campo `value` | `public/help/<idioma>/index.json`, campo `document` |
| `01_Mantenimientos/00_Personas/00_Personas.md` (carpetas numeradas) | `applications/list.md`: inglés, minúsculas y guiones; el orden lo da el índice en los dos casos |
| `core/help/` (`HelpDialog`, `HelpStore`) | `core/components/help-modal/`: `HelpModal`, `HelpModalService` (estado, como `HelpStore`) y `HelpContentService` (índice, documentos y búsqueda) |
| `core/help/help-topics.ts` (`HELP_TOPICS`) | `core/components/help-modal/help-topics.ts` (D-08) |
| `app.routes.ts`: `data: helpTopic(HELP_TOPICS.persons)` | La ruta de cada área: `applications.routes.ts` (lista y alta, ya hecho), `maintenances.routes.ts`, `documentation.routes.ts`, `systems.routes.ts` |
| Diálogos con `DialogService.open(X, { data: helpTopic(...) })` | Diálogos `<p-dialog>` en plantilla: `crud-entity-dialog` con su entrada `helpTopic`; los demás, con `HelpModalService.registerDialogTopic` mientras están abiertos (D-08) |
| Botón ❓ de `shared/components/dialog-header` | Cabecera de `shared/components/crud-entity-dialog` y de los diálogos de la ficha |
| «Centre d'ajuda» en el menú de usuario | Botón «Obrir ajuda» del menú de usuario |
| `app.menu.ts` (`OpenngIcons.USERS` → `pi pi-users`) | `core/components/main-layout/menu-items.ts` y `MAINTENANCE_TABS` (D-21) |
| Índice con `p-tree` | Navegación nativa (D-12); el `p-tree` se descartó por sus fallos de ARIA (D-18) |
| Capturas: MCP chrome-devtools, `scripts/help/capture.js`, `scripts/help/webp.mjs` (ffmpeg), inicio de sesión manual | `npm run help:screenshots`: `e2e/help-screenshots/capture.ts`, WebP de Chromium, inicio de sesión del fixture de e2e (D-07, D-22) |
| `help.mask` para tapar datos personales | Datos de ejemplo creados por API y borrados al terminar; recorte sin la cabecera, que muestra el usuario |
| `scripts/help/pending.mjs` y `scripts/help/revision.json` | `scripts/help/` (por crear, D-23) |
| `READMES/Bugs_Detectados.md` | Apartado «Bugs de la app detectados» de `e2e/COVERAGE.md` (D-24) |
| No se documenta la Consola SQL | Solo pantallas que existen (D-13) |
| Áreas: Manteniments, Eines | Áreas: Aplicacions (lista y ficha), Manteniments; Documentació cuando tenga contenido |
| Tema oscuro, idioma, tamaño de letra | Idioma y tamaño de letra; no hay tema oscuro (D-04) |

Lo que INVAI hace distinto a propósito:

- **Foco al elegir un resultado**: RUPER lo deja en el buscador y lo vacía; INVAI lo lleva al
  encabezado del tema (fase 3), para que un lector de pantalla empiece a leer lo que se ha pedido.
- **Capturas**: se generan con un script repetible en lugar de a mano (D-07).
- **Dos idiomas** (D-14).

Reglas de RUPER que se adoptan tal cual:

- Todo va dirigido a quien usa la aplicación: nada de notas sobre cómo se ha escrito, `TODO` ni
  nombres técnicos de ficheros.
- Lo que se cuenta se comprueba en la app real, campo por campo: qué es obligatorio cambia con el
  tiempo.
- No se quita una captura ni una función documentada sin un reemplazo igual o mejor.
- Un commit obliga a revisar la ayuda si cambia algo visible: una pantalla o una opción de menú,
  una columna o un filtro, un campo o si es obligatorio, una acción, una pestaña o los permisos.

## Dudas para el equipo

Lo que no se puede deducir del código ni comprobar en la app. Hasta que se resuelvan, el tema no
lo cuenta (D-16). Las cuatro primeras eran notas `<!-- REVISAR -->` de la 5.1, que se quitaron de
los temas en su revisión:

- `applications/list.md`: en qué campos busca la búsqueda rápida (código, prefijo, nombre…).
- `applications/list.md`: qué secciones de la ficha cuentan para que una aplicación salga como
  «incompleta».
- `applications/list.md`: qué es una discrepancia DIR3 y qué debe hacer el usuario cuando las hay.
- `applications/create.md`: cómo se asignan después los roles de Soffid si la asignación falla al
  dar de alta.
- `applications/list.md` y `applications/create.md`: quién puede dar de alta aplicaciones y validar
  el DIR3 de todas. La app muestra los botones a todo el mundo; si el back-end los limita por rol,
  hay que decirlo en «Condicions i permisos».

## Fases

### Fase 1 · Base técnica

- [x] Mover el componente de `core/components/user-menu/components/help-modal` a
      `core/components/help-modal` y exportarlo con nombre (`HelpModal`).
- [x] Carpeta por idioma (`help/ca/`, `help/es/`) elegida con `LOCALE_ID`.
- [x] Índice tipado (`HelpTopic`) convertido a `MenuItem` en el componente.
- [x] Índice y documentos cacheados con `cachedRequest`: una petición compartida, reutilizada y
      reintentada tras un error. Antes se pedía `index.json` en cada tecla de la búsqueda.
- [x] Un índice que no carga deja el menú vacío en vez de romper el componente.
- [x] Rutas de imagen resueltas con `URL` respecto al documento (admite `../` y URL absolutas).
- [x] `github-markdown-light.css`.
- [x] Primer tema real en los dos idiomas: «Com fer servir l'ajuda» / «Cómo usar la ayuda».
- [x] Tests unitarios del servicio y del componente.

### Fase 2 · Búsqueda

- [x] Cancelar la búsqueda anterior al escribir: una respuesta tardía de un término anterior se
      descarta.
- [x] Fragmento del resultado como texto plano (antes se pintaba Markdown, con encabezados y
      listas, dentro de una opción del desplegable), cortado por palabras y con `…` en los cortes.
- [x] Estado de carga: lo muestra el propio AutoComplete con su spinner; se quita `isSearching`,
      que no se usaba.
- [x] La región oculta del AutoComplete anunciaba «No selected item» en inglés; ahora dice
      «Cap tema seleccionat» / «Ningún tema seleccionado». En la fase 3 se sustituyó por el número
      de temas encontrados.
- [x] Tests de la búsqueda (`help-search.spec.ts` y ampliaciones del servicio y del componente).
- [x] D-17 (05/11/2026): búsqueda por palabras en `searchDocuments` (`help-search.ts`). Las
      palabras de una letra no cuentan («i», «k»); si la consulta solo tiene de esas, se busca tal
      cual. Primero los temas con todas las palabras en el título y después los que más las
      repiten; en un empate, el orden del índice. El fragmento marca la consulta entera si el texto
      la tiene («Ctrl + K») o, si no, la primera palabra que aparece; si las palabras solo están en
      el título, enseña el principio del texto.
- [x] Cada resultado muestra su grupo («Primers passos») y su nombre accesible lo incluye
      («Com fer servir l'ajuda, Primers passos»), para distinguir temas con el mismo título (la
      pestaña General de la ficha y la de Manteniments). D-11 se amplía así.
- [x] El primer resultado queda activo en cuanto salen las sugerencias (`autoOptionFocus`):
      **Intro** lo abre y las flechas recorren los demás.
- [x] Un documento que no carga se busca solo por su título; antes un solo error dejaba la
      búsqueda sin resultados.
- [x] Tests: `searchWords`, `searchDocuments` (palabras sueltas, orden, empates, nombre, fragmento,
      solo título), el documento que falla en el servicio, el grupo y el primer resultado activo
      en el componente. e2e: buscar «CERCADOR» y abrirlo con **Intro**; buscar «aplicacio alta» y
      que salga primero «Alta d'una aplicació».
- [x] «Com fer servir l'ajuda» / «Cómo usar la ayuda» explican la búsqueda por palabras y el orden;
      captura del buscador repetida en castellano.
- [x] e2e ejecutados el 05/11/2026 con `npm run local`: `help-center.spec.ts` 10/10 dos veces
      seguidas; escaneo Axe de la ayuda sin infracciones. Captura del buscador repetida en catalán.

### Fase 3 · Estados y accesibilidad

- [x] El diálogo no tenía nombre accesible: la cabecera propia no enlazaba el `id` que PrimeNG pasa
      a la plantilla. El botón de cerrar se llama «Tanca el centre d'ajuda».
- [x] Estados del índice (cargando, error con «Reintentar», sin temas) y del documento (sin tema,
      cargando, error con «Reintentar»). `aria-busy` mientras cargan; los errores se anuncian con
      `role="alert"` una sola vez. Un tema nuevo sustituye la petición del anterior.
- [x] Al elegir un tema, el foco va a su encabezado (`tabindex="-1"`); al reabrir el diálogo no se
      mueve y queda en el buscador.
- [x] Al cerrar, el foco vuelve al elemento que lo tenía al abrir (botón «Obrir ajuda» o donde se
      pulsó Ctrl + K), si sigue en la página.
- [x] La ayuda usa la fuente de la app y sigue el selector de tamaño de letra (`1rem` en lugar de
      los 16px fijos de `github-markdown-css`). Los estilos de Markdown solo se aplican al documento,
      no a los mensajes de estado.
- [x] El mensaje «Seleccioneu un tema…» deja de ser un `h3`: no es un encabezado.
- [x] e2e `help-center.spec.ts` (los dos tests de ayuda de `shell.spec.ts` se mueven aquí, incluido
      el `test.fixme` del nombre del diálogo) y escaneo Axe del diálogo vacío y con un tema en
      `accessibility-scan.spec.ts`.
- [x] El buscador deja de usar `forceSelection`: al cerrarse las sugerencias (Escape, Tab o escribir
      rápido) borraba cualquier texto que no fuera exactamente una opción. Lo detectó el e2e.
- [x] Al elegir un resultado del buscador, el foco va al encabezado del tema cuando el AutoComplete
      ya ha cerrado sus sugerencias (antes devolvía el foco al campo después de moverlo nosotros).
- [x] e2e ejecutados el 05/11/2026 con `npm run local`: `help-center.spec.ts` 7/7; escaneo Axe del
      diálogo vacío sin infracciones.
- [x] El PanelMenu daba `list (serious)` en el escaneo Axe con un grupo desplegado: pintaba un
      `<ul>` sin `<li>` cuyo único hijo era el `<ul role="tree">`, sin Pass Through. Se sustituye por
      la navegación nativa de D-12 (`help-topic-tree.ts`, `help-modal.scss`) tras descartar el
      `p-tree` (D-18).
- [x] Tests del índice: apertura solo de la rama activa, botón que muestra y oculta su lista, un solo tema actual,
      el grupo plegado de un tema abierto desde fuera se abre solo, índices de tres niveles.
- [x] e2e: abrir un tema desde la lista y verlo marcado; plegar y desplegar un grupo con **Intro**
      y **Espai**. El escaneo Axe añade el paso con un grupo plegado.
- [x] «Com fer servir l'ajuda» / «Cómo usar la ayuda» explican que la ayuda se abre en el tema de
      la pantalla y cómo se usa la lista; su captura muestra la lista con el tema marcado.
- [x] e2e ejecutados el 05/11/2026 con `npm run local`: `help-center.spec.ts` 9/9 tres veces
      seguidas (`--repeat-each=3`). Escaneo Axe sin infracciones en sus tres pasos: tema de la
      pantalla, otro tema elegido y un grupo plegado.
- [x] Un e2e fallaba a veces: pulsaba un tema antes de que el diálogo acabara de abrirse, y
      PrimeNG, al terminar la animación, pasaba el foco al buscador y se lo quitaba al encabezado.
      Los tests esperan ahora a que el foco esté en el buscador, como haría una persona.
- [x] D-19 (05/11/2026): `HelpContentService.resolveLink` decide adónde lleva cada enlace de un
      tema. Un `.md` del mismo idioma, con su ruta relativa al tema, se abre dentro de la ayuda y el
      foco va a su encabezado; con `#ancla`, al encabezado que nombra (por su texto, como GitHub:
      `headingAnchor`, porque el saneado quita los `id`), en el mismo tema o en otro. Los enlaces a
      otras webs se abren en una pestaña nueva con el icono y el texto oculto «(s'obre en una
      pestanya nova)» del resto de la app. Antes un enlace a otro tema sacaba al usuario de la app.
      A los enlaces de la ayuda se les pone su dirección real, para que abrirlos en otra pestaña o
      copiarlos funcione; Ctrl, Mayús o el botón central se dejan al navegador.
- [x] Primer enlace real: «Alta d'una aplicació» lleva a «Llista d'aplicacions».
- [x] Tests: `resolveLink` (otro tema, con ancla, solo ancla, otra web, una pantalla de la app, una
      imagen, el otro idioma, una dirección inválida), `headingAnchor`, y en el componente abrir un
      tema enlazado, ir a un ancla del mismo tema y de otro, y el enlace externo. e2e: abrir
      «Llista d'aplicacions» desde el enlace de «Alta d'una aplicació» sin cambiar de página.
- [x] e2e ejecutados el 05/11/2026 con `npm run local`: `help-center.spec.ts` 11/11 dos veces
      seguidas; escaneo Axe de la ayuda sin infracciones.
- [x] Revisión automática del 06/11/2026 (árbol de accesibilidad con Playwright): nombre y
      descripción del diálogo, índice como listas anidadas de botones con `aria-expanded`,
      encabezados h2–h5, textos alternativos, nombre de cada resultado con su grupo, foco al título
      tras <kbd>Intro</kbd> y enlaces entre temas. Reflujo a 320 px y espaciado de texto (WCAG
      1.4.10 y 1.4.12) sin desbordamiento horizontal.
- [x] El buscador no anunciaba cuántos temas encuentra (WCAG 4.1.3) y su región de estado decía
      «Tema seleccionat» en cuanto se escribía, porque el texto escrito es el valor del campo. Ahora
      los mensajes de selección de PrimeNG están en blanco y la ayuda tiene su propia región de
      estado: «7 temes trobats», «1 tema trobat» o «No s'han trobat resultats». Test unitario y
      e2e (`help-center.spec.ts` 14/14) y escaneo Axe sin infracciones.
- [ ] Paso manual con NVDA o Narrador, siguiendo [SCREEN-READER.md](SCREEN-READER.md).

### Fase 3b · Textos ocultos de PrimeNG en catalán y castellano

Decidido el 05/11/2026: se traducen para toda la app en `primeng-locale.config.ts`, no solo en la
ayuda.

- [x] `primeng-locale.config.ts` (06/11/2026): las 55 claves `aria` de PrimeNG en catalán y
      castellano. PrimeNG sustituye el objeto `aria` entero en lugar de mezclarlo, así que se
      traducen todas, se usen o no: una que faltara dejaría su control sin nombre. Ahora el botón de
      maximizar se llama «Maximitza», la lista de sugerencias «Llista d'opcions» y la paginación
      «Files per pàgina», «Pàgina següent», «Pàgina 2»…
- [x] Mensajes ocultos y botones de selects, autocompletes y calendarios: `emptyMessage`,
      `emptySearchMessage`, `searchMessage` («{0} resultats disponibles»), `selectionMessage`,
      `emptySelectionMessage`, los botones del calendario (mes, año, década) y los días abreviados.
      64 unidades nuevas en `messages.xml` y `messages.es.xml`, añadidas a mano.
- [x] `primeng-locale.config.spec.ts`: comprueba que están todas las claves `aria` de PrimeNG, que
      ninguna se queda en inglés y que se conservan los marcadores `{0}`, `{star}` y `{page}`.
- [x] Componentes revisados: ningún test dependía de los textos en inglés. Lo único visible que
      cambia es el aviso de lista vacía de los desplegables que no tenían uno propio.
- [x] Encontrado al escanear: el botón ✕ de `p-dialog` solo tiene nombre si se le pasa
      `closeAriaLabel` (no usa la traducción). Cuatro diálogos lo dejaban sin nombre (axe
      `button-name`, crítico): el de errores del servidor, el aviso de cambios sin desar de la
      ficha, el aviso «Totes les responsabilitats estan assignades» y el diálogo de confirmación,
      que ahora nombra el ✕ como su botón de cancelar o, si cerrar no cancela, «Tanca el diàleg».
- [x] Comprobado en castellano en el navegador (`npm run local-es`): «Maximizar», «Lista de
      opciones», «7 temas encontrados», «Primera página», «Filas por página», «No se han encontrado
      resultados». Los botones de página se leían «Página » sin número: en la versión traducida,
      Angular toma `{page}` por sintaxis propia y lo quita. Los marcadores `{0}`, `{star}` y
      `{page}` van ahora como marcadores de `$localize` (`Pàgina ${PAGE}:page:`), con su valor
      añadido en tiempo de ejecución; ya dice «Página 1».
- [x] Cada `p-select` llevaba un botón «dropdown trigger», en inglés y fijo en la plantilla de
      PrimeNG. No recibe el foco y el combobox ya anuncia que se despliega, así que un Pass Through
      global en `app.config.ts` lo oculta a los lectores de pantalla.
- [x] Tests unitarios 1683/1683 (uno de Sistemes i BD, solo, por el timeout conocido). Escaneo Axe de la app sin infracciones; el paso de Manteniments ›
      Responsables no se pudo terminar porque el back-end mostraba el error «El servei de cerca de
      personal de Soffid no està…» (entorno, no código).

### Fase 4 · Comprobador de consistencia y mantenimiento

- [x] `npm run help:check` (`scripts/help/check.mjs`, sin dependencias): mismo índice en `ca` y
      `es` (documentos, grupos e iconos en el mismo orden), iconos sin repetir entre hermanos (D-21),
      cada tema existe y empieza con `###` y su etiqueta, ningún `.md` ni imagen sin usar, las
      mismas capturas en los dos idiomas, texto alternativo en cada imagen, peso (error por encima
      de 70 KB, aviso por encima de 40 KB, D-22), enlaces a temas y apartados que existen (D-19), y
      nada de comentarios HTML (D-16), `#`/`##`, `---` ni emojis (D-15). Probado sobre una copia
      con un error de cada tipo (`-- --root`): los encuentra todos y sale con 1. La ayuda actual
      pasa con dos avisos: `list.webp` pesa 45 y 46 KB.
- [x] D-23: `npm run help:pending` (`scripts/help/pending.mjs`) y `scripts/help/revision.json`,
      que asigna a cada tema sus carpetas de código y guarda el último commit revisado (`b7c7a1f`).
      `-- --mark` lo avanza hasta `HEAD`. Probado desde un commit anterior: agrupa los cambios por
      tema y deja aparte los de la pestaña Documentació, que aún no tiene tema.
- [x] Documentados en [TEMPLATE.md](TEMPLATE.md), con los pasos para cerrar una tanda de temas.
- [x] CI: decidido el 06/11/2026 que `help:check` se queda en local; no entra en `.gitlab-ci.yml`.

### Fase 5 · Contenido por área

Una subfase por área del menú principal, con sus capturas en los dos idiomas. Cada tema se escribe
a partir de las plantillas, los textos y las validaciones del código y se comprueba en la app; lo
que dependa del negocio (qué significa una clasificación ENS, quién debe rellenar qué) va a
[Dudas para el equipo](#dudas-para-el-equipo). Orden confirmado el 05/11/2026:

- [x] 5.0 · Capturas y plantilla:
  - [x] Plantilla y guía de estilo en [TEMPLATE.md](TEMPLATE.md).
  - [x] `npm run help:screenshots`: proyecto `help-screenshots` de Playwright
        (`e2e/help-screenshots/`), que solo existe cuando se lanza con ese script, así que
        `npm run test:e2e` no lo ejecuta. Detecta el idioma por la carpeta que pide la app,
        recorta al elemento que se explica y guarda WebP codificado por Chromium.
  - [x] «Com fer servir l'ajuda» / «Cómo usar la ayuda» pasan a la plantilla, con dos capturas
        (`help-centre.webp`, `help-search.webp`).
  - [x] En el texto para buscar, cada encabezado acaba en punto: el fragmento decía «Qué
        encontrará Arriba, el buscador…».
  - [x] Capturas en castellano (con `npm run local-es`).
  - [x] Capturas en catalán (con `npm run local`).
  - Las capturas del centro de ayuda se repitieron el 05/11/2026 con el índice nativo (D-12), en
    los dos idiomas. La de la lista recorta hasta el final del índice
    (el `nav` se estira hasta la altura del tema) y la del buscador deja solo 4 px bajo las
    sugerencias (`saveHelpImage` admite `margin`).
- [x] 5.0b · D-25: plantilla y temas de «Primers passos» a la manera de RUPER.
  - [x] [TEMPLATE.md](TEMPLATE.md) reescrita como la guía de RUPER adaptada a INVAI: idioma,
        estructura de los temas de pantalla (D-15) y de los generales, reglas (D-16), Markdown
        (D-20), enlaces (D-19), carpetas e índice (D-21), capturas y datos personales, y
        mantenimiento (D-23, D-24). Incluye una plantilla para copiar.
  - [x] «Com fer servir l'ajuda» / «Cómo usar la ayuda» con la estructura de tema general:
        Objectiu, Com s'hi accedeix, Cercar, Índex, Enllaços, Mida de la finestra, Coses a tenir
        en compte. Teclas con `<kbd>`.
  - [x] «Capçalera i menú d'usuari» / «Cabecera y menú de usuario»
        (`getting-started/header-and-user-menu.md`): capçalera, ruta de navegació, menú lateral
        (con el submenú de Manteniments y el menú plegado), menú d'usuari y peu de pàgina. Tres
        capturas: `header.webp` (con «Usuari de proves» en lugar del nombre real, `maskText`),
        `sidebar.webp` (sin la página detrás) y `user-menu.webp`.
  - [x] «Trucs de la interfície» / «Trucos de la interfaz» (`getting-started/interface-tips.md`):
        barra de las listas, filtros, columnas, tablas, formularios, desplegables y fechas, avisos
        y atajos de teclado. Dos capturas: `toolbar.webp` y `columns.webp`.
  - [x] Los dos temas nuevos en los índices de «Primers passos» (`pi pi-user`, `pi pi-lightbulb`).
  - [x] `capture.ts`: `maskText` y la opción `area` de `saveHelpImage` (recorta a varios elementos,
        como un menú y su submenú).
  - [x] La captura del buscador busca ahora «menú d'usuari» / «menú de usuario»: dos resultados, el
        que lo tiene en el título primero. El e2e de la búsqueda usa «MAXIMITZAR», que solo sale en
        un tema.
  - [x] Fallos de la app anotados en `e2e/COVERAGE.md` (D-24), sin corregir: textos del menú
        lateral y de la búsqueda rápida sin traducir, el cambio de idioma pierde la pantalla y el
        `combobox` oculto del selector de columnas vale «[object Object],…».
  - [x] Capturas en castellano.
  - [x] Capturas en catalán. La del centro de ayuda recorta ahora hasta el párrafo que sigue a la
        primera lista: con seis temas, el índice acababa a media línea del texto.
  - [x] e2e ejecutados el 05/11/2026 con `npm run local`: `help-center.spec.ts` 11/11 dos veces
        seguidas. El escaneo Axe de la ayuda añade los dos temas nuevos (listas anidadas, `<kbd>`,
        enlaces) y no da infracciones.
- [ ] 5.1 · Aplicacions: llista, cerca i filtres, accions de la taula, alta d'una aplicació.
  - [x] Temas `applications/list.md` («Llista d'aplicacions») y `applications/create.md` («Alta
        d'una aplicació») en los dos idiomas, con el grupo «Aplicacions» en los índices.
  - [x] `e2e/help-screenshots/applications.spec.ts`: crea por API tres aplicaciones de ejemplo
        (copiando los catálogos de una aplicación activa), las muestra con la búsqueda rápida y las
        da de baja al terminar. Captura la lista, el menú de una fila, los filtros y el formulario
        de alta. Los nombres de los botones se escriben en catalán y se traducen con
        `messages.es.xml` (`uiText`).
  - [x] Tests: las notas `<!-- REVISAR -->` no se ven en el tema ni entran en la búsqueda.
  - [x] Capturas en castellano.
  - [x] Capturas en catalán.
  - [x] Los dos temas pasan a la estructura D-15: Objectiu, Com s'hi accedeix, Accions
        disponibles (con «Validar DIR3 de totes» como subapartado), Cerca i filtres, Taula de
        resultats y Condicions i permisos en la lista; Accions disponibles, Formulari (obligatoris y
        opcionals por separado), Desar y Condicions i permisos en el alta. Filtros, columnas y campos
        en listas, uno por línea; mensajes copiados de la app.
  - [x] Sin notas `REVISAR` (D-16): pasan a [Dudas para el equipo](#dudas-para-el-equipo), y el tema
        solo cuenta lo comprobado.
  - [x] Enlaces: la lista lleva al alta y a «Trucs de la interfície»; el alta, a la lista y a su
        apartado «Desar» (`#desar`), que prueba un e2e nuevo.
  - Las capturas no cambian: la lista, el menú de una fila, los filtros y el formulario siguen
    igual.
  - [x] e2e ejecutados el 05/11/2026 con `npm run local`: `help-center.spec.ts` 12/12 dos veces
        seguidas; escaneo Axe de la ayuda sin infracciones.
  - [ ] Respuesta del equipo a sus dudas (en [Dudas para el equipo](#dudas-para-el-equipo)).
  - El botón de exportar a Excel se menciona en una línea («encara no està implementat»), sin
    describirlo como función.
- [x] 5.2 · Fitxa d'aplicació (I): General, Responsables, Sistemes i BD.
  - [x] Temas `application-detail/overview.md` («Com funciona la fitxa»: cómo se abre, pestañas e
        indicadores de datos incompletos, consultar y editar, permisos por pestaña),
        `general.md`, `responsible.md` y `systems-databases.md`, en los dos idiomas, con la
        estructura D-15. Escritos a partir del código (tres agentes de lectura y revisión propia) y
        comprobados con las capturas.
  - [x] Índice: grupo «Fitxa d'aplicació» dentro de «Aplicacions» (`pi pi-id-card`), con iconos de
        las pestañas equivalentes de Manteniments (`pi pi-cog`, `pi pi-users`, `pi pi-server`).
  - [x] Ayuda contextual: la ruta `:id` abre «Com funciona la fitxa» y cada pestaña documentada, su
        tema; las pestañas sin tema propio (de Desenvolupament a Documentació) caen en el de la
        fitxa hasta la 5.3 y la 5.4. e2e nuevo en `help-center.spec.ts`.
  - [x] `e2e/help-screenshots/application-detail.spec.ts`: crea por API una aplicación de ejemplo,
        dos personas externas ficticias («Joana Exemple», «Pere Exemple») de una empresa real cuyo
        nombre se sustituye por «Empresa d'exemple», y un servidor, un sistema y una base de dades
        de ejemplo; los relaciona, captura y lo da todo de baja al terminar. Las responsabilidades
        que exigen personal CAIB se quedan vacantes: ninguna persona real sale en la ayuda. La
        creación de aplicaciones de ejemplo pasa a `example-applications.ts`, que también usa la
        lista.
  - [x] Capturas en catalán: `overview.webp`, `general.webp`, `responsible.webp` y
        `systems-databases.webp`.
  - [x] Fallos anotados en `e2e/COVERAGE.md` (D-24): Responsables no comprueba el permiso de
        escritura; la baja de una relación de Sistemes i BD cita su identificador interno y sus
        botones tienen nombres genéricos; dos formatos de fecha en General.
  - [x] e2e ejecutados el 06/11/2026 con `npm run local`: `help-center.spec.ts` 13/13 dos veces
        seguidas; escaneo Axe de la ayuda sin infracciones.
  - [x] Capturas en castellano (con `npm run local-es`).
- [x] 5.3 · Fitxa d'aplicació (II): Desenvolupament, Accessibilitat, Seguretat.
  - [x] Temas `application-detail/development.md`, `accessibility.md` y `security.md` en catalán y
        castellano, con la estructura D-15 y revisión de plantillas, textos y validaciones: campos
        obligatorios y opcionales por separado, columnas una por línea, límites de las URL y
        comportamiento de guardar, cancelar y cerrar una ventana de edición.
  - [x] Temas en los dos índices, en `HELP_TOPICS` y en las rutas de las tres pestañas. El e2e
        contextual comprueba el tema, su imagen, el foco en el buscador y la descripción del diálogo;
        la comprobación de una pestaña sin tema propio pasa de Desenvolupament a Integracions.
  - [x] `e2e/help-screenshots/application-detail-technical.spec.ts`: crea por API una aplicación de
        ejemplo, un proveedor ficticio, una tecnología relacionada, un contexto web, una clasificación
        ENS, un riesgo y una medida; usa los catálogos actuales y da de baja los registros al terminar,
        también si falla una comprobación. No asigna roles reales ni incluye datos personales.
  - [x] Capturas `development.webp`, `accessibility.webp` y `security.webp` en los dos idiomas;
        revisadas visualmente y por debajo de 40 KB. `saveHelpImage` amplía temporalmente el viewport
        para evitar el recorte de las pestañas largas por el scroll interior de la app, ajusta la
        calidad de WebP y rechaza imágenes de más de 70 KB.
  - [x] Comprobación real de los tres temas en ambos idiomas: título, imagen cargada y foco inicial;
        Axe WCAG 2.2 AA a 320 px sin infracciones y sin desbordamiento horizontal, también con texto
        al 200% en escritorio. El guion [SCREEN-READER.md](SCREEN-READER.md) refleja los temas nuevos;
        el paso humano con NVDA o Narrador sigue pendiente en la fase 3.
  - [x] Validación del 06/11/2026: `help-center.spec.ts` 13/13 y escaneo Axe de la ayuda sin
        infracciones; generación y comprobación de capturas 1/1 en cada idioma; tipos e2e correctos;
        suite unitaria 1683/1683 y build de producción localizada correcta. La build conserva el aviso
        previo de presupuesto de `section-actions.component.scss`.
  - [x] `npm run help:check`: 0 errores y los dos avisos previos de peso de `applications/imgs/list.webp`.
        Auditoría de `core/components/help-modal`: 0 errores y 0 elementos de revisión.
- [x] 5.4 · Fitxa d'aplicació (III): Dades, Integracions, Documentació.
  - [x] Cuatro temas en catalán y castellano: `application-detail/data.md`, `integrations.md`,
        `documentation.md` y `document-detail.md`. La ficha propia del documento tiene tema propio
        porque permite editar sus datos y gestionar las versiones fuera de las pestañas de la app.
  - [x] Índices, enlaces desde «Com funciona la fitxa», constantes `HELP_TOPICS`, ayuda contextual
        en las tres pestañas y en `:id/documentation/:documentId`, y carpetas en `revision.json`.
  - [x] Contenido contrastado con plantillas, validaciones, traducciones y contratos: URL manuales
        de Dades, conexiones y roles de Soffid, filtros y columnas de documentos y enlaces, altas,
        versiones, estados y límites del correo. Catálogos documentales comprobados con SELECT en
        `LocalOracle`, `XEPDB1`, esquema `INVAI`: `INV_LKUP_DOCUMENT_STATUS` y `INV_LKUP_MIME_TYPE`.
  - [x] `e2e/help-screenshots/application-detail-information.spec.ts`: un test de capturas y otro
        de validación, con ejemplos independientes y limpieza de aplicación, categorías, documento,
        enlace y conexión. Dos versiones de un fichero de texto real; usuario y autores anonimizados.
        La ventana de correo se rellena con un destinatario ficticio y se cierra sin enviar.
  - [x] Capturas en catalán: `data.webp`, `integrations.webp`, `documentation.webp`,
        `documentation-email.webp` y `document-detail.webp`, revisadas y por debajo de 40 KB.
  - [x] Ayuda en catalán: los cuatro temas abren desde sus pantallas, las imágenes cargan y el foco
        queda en el buscador. Axe a 320 px sin infracciones, sin desbordamiento horizontal y con
        reflujo comprobado al 200% en escritorio (`expectScreenHelp` de `capture.ts`).
  - [x] Las cinco capturas y la comprobación equivalente de los cuatro temas en castellano,
        revisadas visualmente y por debajo de 40 KB. Vista conjunta local en
        `tmp/help-5.4-preview.png`.
  - [x] La generación de capturas desactiva live reload y HMR para que escribir una imagen no cierre
        la ventana siguiente; las pruebas normales conservan su servidor habitual.
  - [x] Incidencia registrada en `e2e/COVERAGE.md`: el diálogo compartido de correo tiene título
        visible pero no nombre accesible; se localiza por el encabezado sin corregir la app (D-24).
        También se registra que la ficha del documento puede usar los nombres catalanes de categoría
        y subcategoría en castellano; las capturas usan las etiquetas ficticias de cada idioma.
  - [x] Suite unitaria 1683/1683 y auditoría de ayuda y rutas: 0 errores y 0 elementos de revisión.
  - [x] Validación del 06/11/2026: `help-center.spec.ts` 13/13 y escaneo Axe de la ayuda sin
        infracciones (14 pruebas en total); captura y validación localizada correctas en cada idioma;
        tipos e2e correctos. `help:check`: 0 errores y los dos avisos previos de la lista.
        Build de producción en ambos idiomas correcta, con el aviso previo de presupuesto de
        `section-actions.component.scss`. El paso humano con NVDA o Narrador sigue pendiente en la
        fase 3; [SCREEN-READER.md](SCREEN-READER.md) incluye las nuevas pantallas.
- [x] 5.5 · Manteniments (I): General (categories, sistemes d'informació, àmbits, comissió
      informàtica) i Responsables (traspàs de rols, persones, autoritzacions, empreses).
  - Diez temas en catalán y castellano: dos vistas generales y ocho apartados, con veinte
    imágenes WebP revisadas. Formularios vacíos sin datos personales; transferencia en estado
    inicial con reintento de búsqueda, debido a la indisponibilidad local de Soffid.
  - Contexto en las dos rutas y botón/atajo de ayuda en los siete formularios CRUD. Índices,
    enlaces y seguimiento de fuentes actualizados.
  - Validación del 06/11/2026: captura y validación Playwright correctas en cada idioma;
    apertura por botón y teclado, retorno de foco, imágenes localizadas y comprobación Axe,
    reflujo a 320 px y texto al 200 % en los dos temas de ruta y siete temas de formulario.
    El tema de transferencia se comprueba desde su enlace e imagen; no se ejecutan operaciones
    reales de transferencia o revocación. Incidencia de Soffid anotada en `e2e/COVERAGE.md`.
  - 221 archivos / 1683 tests unitarios correctos y tipos e2e correctos. `help:check`: cero
    errores y los dos avisos previos de imágenes. Auditoría de accesibilidad del ámbito: cero
    errores; auditoría de tablas conserva el hallazgo previo de document-category-permissions-table,
    fuera de este bloque. El paso humano con lector de pantalla sigue pendiente en fase 3.
- [x] 5.6 · Manteniments (II): Sistemes, Desenvolupament, Accessibilitat, Seguretat,
      Integracions i Documentació.
  - Siete temas en catalán y castellano: las seis pantallas completas y el detalle de categoría
    documental. Incluyen los dieciocho apartados, campos obligatorios/opcionales, filtros,
    columnas, consulta/edición, baja/restauración y permisos de las categorías.
  - Cincuenta capturas WebP revisadas (25 por idioma), todas por debajo de 40 KiB: seis vistas
    generales, dieciocho altas vacías y una categoría documental de ejemplo. La categoría se
    crea y se da de baja en cada prueba; no se cambian permisos ni registros existentes.
  - Contexto en siete rutas y en los formularios de todos los apartados. El diálogo de
    subcategorías abre el tema del detalle; el alta de categorías abre Documentación.
    Los índices y `scripts/help/revision.json` incluyen los nuevos temas y sus fuentes.
  - Validación del 06/11/2026: 7/7 capturas y 7/7 recorridos de ayuda en cada idioma;
    apertura por botón y Ctrl+K, foco inicial y devolución, imágenes localizadas, Axe en la
    ayuda, reflujo a 320 px y texto al 200 %. La prueba espera al apartado abierto y limita
    el botón de alta a ese apartado para evitar capturar el anterior durante la transición.
  - Regresión del centro de ayuda: 13/13. Suite unitaria: 221 archivos / 1683 tests correctos;
    tipos e2e y build de producción en ambos idiomas correctos. `help:check`: cero errores y
    los dos avisos previos de imágenes. La build conserva el aviso de presupuesto de
    `section-actions.component.scss`.
  - Auditoría de accesibilidad: 218 archivos, cero errores y el aviso previo de casillas de
    permisos deshabilitadas en consulta. Auditoría de tablas: el mismo hallazgo de carga en
    document-category-permissions-table que en la línea base. Ambos quedan en `e2e/COVERAGE.md`;
    no se han cambiado los contratos de edición, carga ni estilos de formularios/tablas.
    El paso humano con NVDA/Narrador sigue pendiente en fase 3 y se amplía SCREEN-READER.md.

La plantilla de tema y las reglas de las capturas están en [TEMPLATE.md](TEMPLATE.md).

### Fase 6 · Ayuda contextual

D-08: deja de ser opcional y se hace antes de la 5.2. Empezada el 05/11/2026.

- [x] El estado de la ayuda (abrir, cerrar, qué tema enseñar, devolver el foco) se separa del
      contenido: `HelpModalService` solo depende de la página y del router, y `HelpContentService`
      carga el índice, los documentos y la búsqueda. Así cualquier diálogo puede abrir la ayuda
      sin arrastrar `HttpClient` a sus tests (en RUPER, `HelpStore` y el diálogo).
- [x] `HELP_TOPICS` en `core/components/help-modal/help-topics.ts` y un test que comprueba que
      todos están en los dos `index.json`. Hoy: `applications/list.md` y `applications/create.md`.
- [x] Las rutas declaran su tema con `data: helpTopic(HELP_TOPICS.x)`: la lista y el alta de
      aplicaciones. Una pantalla sin tema propio usa el de la que la contiene (la ruta activa más
      profunda que tenga uno).
- [x] `open()` elige el tema del último diálogo abierto, si no el de la pantalla y, si tampoco, deja
      el último consultado. Solo lo decide al abrir, no mientras la ayuda ya se ve.
- [x] Al abrirse en un tema, el foco se queda en el buscador (<kbd>Ctrl</kbd> + <kbd>K</kbd> es para
      buscar) y el diálogo se describe con «Tema obert: …» (`aria-describedby` a un texto oculto),
      que los lectores de pantalla leen al entrar. Un tema que ya se ve no se vuelve a cargar.
- [x] `crud-entity-dialog` acepta `helpTopic`: mientras se ve, <kbd>Ctrl</kbd> + <kbd>K</kbd> abre
      su tema, y la cabecera muestra un botón «Obrir ajuda» con `aria-keyshortcuts="Control+K"`.
      Usado por los formularios documentados en las fases 5.2–5.6.
- [x] El botón «Obrir ajuda» del menú de usuario también anuncia el atajo.
- [x] Tests unitarios: servicio (pantalla, pantalla contenedora, sin tema, diálogos anidados,
      diálogos con el mismo tema), componente (tema pedido sin mover el foco, descripción, sin
      recarga), diálogo CRUD (botón, tema mientras se ve, al destruirse) y `HELP_TOPICS`.
- [x] e2e en `help-center.spec.ts`: desde la lista se abre en «Llista d'aplicacions» con el foco
      en el buscador y la descripción del diálogo; desde el alta, en «Alta d'una aplicació».
- [x] e2e ejecutados el 05/11/2026 con `npm run local`: `help-center.spec.ts` 8/8. Escaneo Axe:
      el diálogo abierto en el tema de la pantalla, sin infracciones (el paso se llamaba
      «vacío» y ahora es `help-centre-screen-topic`); con un grupo desplegado seguía el `list` del
      PanelMenu, que resuelve la navegación nativa (D-12).
- [x] Los avisos propios de la ficha (cambios sin guardar y responsabilidades asignadas) tienen
      botón de ayuda al tema de la pestaña activa, el mismo que usa Ctrl+K.
- [x] Cabeceras de pantalla con botón de ayuda compartido, actualizado al cambiar de ruta o
      pestaña. Los CRUD mantienen su tema propio y usan el de la sección cuando no declaran uno.
- [x] Índice y artículo con scroll independiente, buscador fijo y regiones accesibles con teclado,
      también en móvil. Escape cierra la ayuda sin cerrar el formulario que queda debajo.
- [x] Cada apertura reinicia el índice plegado salvo los antecesores del tema activo, incluidos
      los subgrupos. Mientras la ayuda permanece abierta se pueden desplegar otras ramas;
      buscar o seguir un enlace revela la rama del tema elegido.
  - Validación del 06/11/2026: 1.686 tests unitarios, 13 regresiones del centro de ayuda y los
    dos escenarios nuevos en catalán y castellano. Cabeceras a 320, 768 y 1440 px, reflujo de la
    ayuda a 320 px y texto al 200%, foco de apertura/retorno y Axe sin infracciones en las
    superficies comprobadas. Compilación localizada correcta; continúa el aviso previo de
    presupuesto CSS de `section-actions` (6,63 kB).
  - Skills aplicadas: accesibilidad, layout y formularios. Auditoría de ayuda, cabecera, diálogo
    CRUD y aplicaciones: 257 archivos, cero errores y los dos avisos previos de filas clicables
    (ambas conservan acción con Intro y control nativo de selección). Sin cambios en política
    readonly/disabled ni nuevas excepciones. Se conserva la excepción previa de contraste de
    bordes de formulario y sigue pendiente la revisión humana con NVDA de la fase 3.
  - La suite completa se ejecutó con `VITEST_MAX_WORKERS=4`: la concurrencia predeterminada
    produjo timeouts de 5 s en pruebas existentes de filtros, que pasan con ese límite.
