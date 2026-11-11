# Temas de ayuda: cómo se escriben, se capturan y se mantienen

Guía para escribir los documentos del **Centre d'ajuda** de INVAI, adaptada de la de RUPER
(`ruper-front/READMES/Ayuda.md`). Las decisiones de fondo y su estado están en [PLAN.md](PLAN.md);
aquí se citan por su número (D-xx).

La ayuda se abre con <kbd>Ctrl</kbd> + <kbd>K</kbd> o desde **Obrir ajuda** del menú de usuario,
directamente en el tema de la pantalla o del diálogo en el que se está (D-08).

## Contenido

### Idioma (D-14)

Cada tema se escribe dos veces: en catalán (`public/help/ca/`) y en castellano
(`public/help/es/`). Cada versión cita la interfaz de su idioma: el tema en catalán dice **Desar** y
el castellano, **Guardar**. Los nombres se copian de la pantalla o de `src/locale/messages.es.xml`,
nunca se traducen a mano.

- **Catalán**: imperativo de vosaltres, como la interfaz («Seleccioneu», «Feu clic», «Premeu»).
- **Castellano**: usted («Seleccione», «Haga clic», «Pulse»).
- Frases cortas y sin jerga técnica: nada de «endpoint», «DTO» o «modal» («diàleg», «finestra»).

### Estructura de un tema de pantalla (D-15)

Un encabezado `###` con el nombre que el tema tiene en el índice (el título del diálogo de la
ayuda ya es `h2`) y, con `####`, los apartados que apliquen, en este orden:

| Apartado | Catalán | Castellano | Qué cuenta |
| --- | --- | --- | --- |
| Objetivo | Objectiu | Objetivo | Una o dos frases sobre qué permite hacer la pantalla. |
| Acceso | Com s'hi accedeix | Cómo se accede | El camino por el menú, una captura del estado inicial y, si la tiene, el enlace al tema de la pantalla de la que se viene. |
| Acciones | Accions disponibles | Acciones disponibles | Todos los botones e iconos, uno por línea. |
| Búsqueda | Cerca i filtres | Búsqueda y filtrado | Todos los campos del buscador y de la búsqueda rápida. |
| Tabla | Taula de resultats | Tabla de resultados | Las columnas, las ocultas por defecto, el orden y cada acción del menú de la fila. |
| Formulario | Formulari | Formulario | Un subapartado `#####` por operación (alta, modificación…), con los campos obligatorios y los opcionales en listas separadas. |
| Guardar | Desar | Guardar | Qué pasa al guardar y cuándo no se puede. |
| Condiciones | Condicions i permisos | Condiciones y permisos | Qué hace falta para que una acción esté disponible: rol, estado, modo consulta o edición. |

Saltarse un apartado que no aplica está bien; saltarse uno que aplica, no. La pantalla se documenta
**entera**: todas las acciones, todos los campos, todas las opciones de cada menú.

### Estructura de un tema general

Los temas de «Primers passos» no describen una pantalla sino algo que sirve en todas (la ayuda, la
cabecera, los trucos de la interfaz). Llevan **Objectiu / Objetivo** y **Com s'hi accedeix / Cómo se
accede**, y después un apartado por función («Cercar», «Índex», «Enllaços»…). Si hace falta, cierran
con **Coses a tenir en compte / Tenga en cuenta**.

### Reglas

- Todo va dirigido a quien usa la aplicación. Nada de notas sobre cómo se ha escrito, `TODO`,
  comentarios HTML ni nombres de ficheros (D-16). Los `.md` son ficheros estáticos que se publican
  tal cual: lo que no se puede comprobar va a «Dudas para el equipo» de [PLAN.md](PLAN.md) y el
  tema no lo cuenta.
- Lo que se cuenta se comprueba en la **app real**, campo por campo, además de en el código: qué
  es obligatorio cambia con el tiempo.
- Los nombres de menús, botones, pestañas y campos van en **negrita** y exactamente como aparecen
  en pantalla en ese idioma. Si un botón solo tiene icono, se describe el icono («el botó de
  l'embut») y se da su nombre accesible si ayuda a encontrarlo.
- Las enumeraciones de campos u opciones van en listas, una por línea.
- No se quita una captura ni una función documentada sin un reemplazo igual o mejor.
- Solo se documentan pantallas y funciones que existen (D-13). Un botón que todavía no hace nada se
  dice en una línea, sin describirlo como función.
- Sin emojis ni separadores `---`: los lectores de pantalla los leen («dardo», «separador»).

### Markdown (D-20)

Se admiten tablas, `<kbd>` y `<details>`. Las teclas van con `<kbd>`: <kbd>Ctrl</kbd> +
<kbd>K</kbd>, <kbd>Intro</kbd>, <kbd>Esc</kbd>, <kbd>Tab</kbd>, con el nombre del teclado español.
Angular sanea el HTML del documento, así que no se usan comentarios HTML, listas de tareas
(`- [ ]`), estilos en línea ni `id`.

### Enlaces (D-19)

- **A otro tema**: su ruta relativa desde el `.md` actual y, como texto, la etiqueta que tiene en el
  índice. Se abre dentro del centro de ayuda y el foco va a su título.
  `[Llista d'aplicacions](../applications/list.md)`
- **A un apartado**: `#` y el título del apartado en minúsculas, sin acentos ni signos y con guiones,
  como en GitHub: `[Com s'hi accedeix](#com-shi-accedeix)`. En otro tema:
  `[filtres](list.md#cerca-i-filtres)`.
- **A otra web**: con su dirección completa. Se abre en una pestaña nueva, con el icono y el aviso
  oculto «(s'obre en una pestanya nova)» que usa el resto de la app.
- **A una pantalla de la aplicación**: no. Se explica el camino por el menú.

### Imágenes

`![Texto alternativo](imgs/nombre.webp)`. El texto alternativo describe lo que se ve y sirve sin la
imagen; no empieza por «captura de».

## Carpetas e índice

```
public/help
├── ca
│   ├── index.json
│   ├── getting-started
│   │   ├── using-help.md
│   │   └── imgs
│   │       └── help-centre.webp
│   └── applications
│       ├── list.md
│       └── imgs
└── es
    └── (el mismo árbol, con los mismos nombres)
```

- Un tema = un `.md` en `ca/` y otro en `es/`, con la **misma ruta**, y una entrada en el
  `index.json` de cada idioma (D-01).
- Rutas en inglés, en minúsculas y con guiones, agrupadas por área: `getting-started/`,
  `applications/`, `application-detail/`, `maintenances/`. El orden lo da el índice.
- Imágenes `.webp` en `imgs/`, junto a su tema (D-06).

`index.json` es una lista de `HelpTopic` (D-02):

```json
[
  {
    "label": "Aplicacions",
    "icon": "pi pi-th-large",
    "items": [
      { "label": "Llista d'aplicacions", "icon": "pi pi-list", "document": "applications/list.md" }
    ]
  }
]
```

- Solo `label`, `icon`, `document` (tema) e `items` (grupo).
- El orden y los iconos de los grupos son los del menú lateral (D-21):
  `core/components/main-layout/menu-items.ts` (`PrimeIcons.TH_LARGE` → `"pi pi-th-large"`), los de
  Manteniments salen de `MAINTENANCE_TABS` y los de la ficha, de sus pestañas. Entre hermanos no se
  repite icono.
- Los temas que se abren desde la app están en `HELP_TOPICS`
  (`core/components/help-modal/help-topics.ts`), y un test comprueba que siguen en los dos índices.
  Una ruta declara el suyo con `data: helpTopic(HELP_TOPICS.x)`; un `crud-entity-dialog`, con su
  entrada `helpTopic`; cualquier otro diálogo, con `HelpModalService.registerDialogTopic` mientras
  está abierto (D-08).

## Capturas (D-07)

`npm run help:screenshots` abre la aplicación con Playwright, inicia sesión con el usuario de
`.env` y guarda cada captura en `public/help/<idioma>/<área>/imgs/`. Los specs están en
`e2e/help-screenshots/` y comparten los ayudantes de `capture.ts` (`openHelp`, `openApp`,
`uiText`, `maskText`, `saveHelpImage`). El idioma lo decide el servidor que esté en el 4200:

- Catalán: `npm run local -- --live-reload=false --hmr=false`.
- Castellano: `npm run local-es -- --live-reload=false --hmr=false`.

Cuando Playwright arranca su propio servidor para las capturas, ya desactiva la recarga automática.

Hace falta el back-end local y la VPN, como en los e2e. Reglas:

- Se recorta al elemento que se explica (diálogo, panel, tabla, menú), nunca la página entera.
- Los datos que salen los crea el propio spec por API (aplicaciones de ejemplo) y los da de baja al
  terminar; nunca personas ni aplicaciones reales.
- Si cambia una pantalla, se vuelve a lanzar el spec en los dos idiomas y se revisan las imágenes
  una a una.
- Peso (D-22): como mucho 40 KB y nunca más de 70 KB; si no baja, se recorta más. `npm run
  help:check` falla por encima de 70 KB y avisa por encima de 40 KB.

La tanda de la ficha de Desarrollo, Accesibilidad y Seguridad se repite con
`npm run help:screenshots -- application-detail-technical.spec.ts`. También comprueba los tres temas
en el idioma del servidor, su imagen, el foco inicial, Axe a 320 px y el reflujo con texto al 200%.
Si se crean imágenes con nombres nuevos mientras `ng serve` está arrancado, puede seguir
respondiendo 404 para ellas: reiniciarlo y repetir la tanda antes de darla por validada.

Para Datos, Integraciones y Documentación, incluido el detalle de un documento y el diálogo de
correo, se usa `application-detail-information.spec.ts`. Al crear las imágenes por primera vez:

1. `npm run help:screenshots -- application-detail-information.spec.ts --grep captures`.
2. Reiniciar el servidor para que descubra los archivos nuevos.
3. `npm run help:screenshots -- application-detail-information.spec.ts --grep validates --no-deps`.

Con las imágenes ya presentes se pueden ejecutar los dos tests juntos, sin `--grep`. La captura
del diálogo de correo prepara un destinatario ficticio y una selección, sin pulsar **Envia**.

### Datos personales

La cabecera muestra el nombre y el usuario de quien hace las capturas. Si una captura tiene que
enseñarla, antes se sustituyen con `maskText` por datos de ejemplo («Usuari de proves»): las
imágenes de la ayuda son ficheros estáticos que cualquiera puede descargar.

## Mantenerla al día

Un commit obliga a revisar la ayuda si cambia algo visible: una pantalla o una opción de menú, una
columna o un filtro, un campo o si es obligatorio, una acción, una pestaña o los permisos. Los
cambios internos (servicios, tipos, estilos, pruebas) no.

```bash
npm run help:check
npm run help:pending
```

- `help:check` comprueba que la ayuda sigue esta guía: el mismo índice en los dos idiomas, sin
  iconos repetidos entre hermanos, cada tema empieza con `###` y su etiqueta, ningún archivo ni
  imagen sin usar, texto alternativo y peso de las imágenes, enlaces a temas y apartados que
  existen, y nada de comentarios HTML, `#`/`##`, `---` ni emojis. Falla con un error y avisa del
  resto. Con `-- --root <carpeta>` revisa otra copia.
- `help:pending` lista los commits de `src/app` posteriores a la última revisión, agrupados por el
  tema que describe cada archivo según `scripts/help/revision.json`. Los archivos sin tema salen
  aparte: pueden ser una pantalla nueva.

Al cerrar una tanda de temas:

1. Añadir los temas nuevos a `scripts/help/revision.json` con sus carpetas de código.
2. Pasar `npm run help:check` y comprobar en la app que los temas y sus imágenes se ven.
3. Dar la ayuda por revisada hasta el commit actual: `npm run help:pending -- --mark`.

Los fallos de la aplicación que aparezcan al documentar una pantalla se anotan en el apartado de
bugs de `e2e/COVERAGE.md` y no se corrigen sobre la marcha (D-24).

## Plantilla

```markdown
### <Etiqueta del índice>

#### <Objectiu | Objetivo>

<Qué permite hacer, en una o dos frases.>

#### <Com s'hi accedeix | Cómo se accede>

<Camino por el menú.>

![<Lo que se ve en la pantalla al entrar>](imgs/<nombre>.webp)

#### <Accions disponibles | Acciones disponibles>

- **<Botón>**: <qué hace>.

#### <Cerca i filtres | Búsqueda y filtrado>

- **<Filtro>**: <qué busca>.

#### <Taula de resultats | Tabla de resultados>

- **<Columna>**

#### <Formulari | Formulario>

##### <Alta | Modificació…>

Obligatoris:

- **<Campo>**: <formato o límite>.

Opcionals:

- **<Campo>**

#### <Desar | Guardar>

<Qué pasa al guardar y qué mensaje aparece.>

#### <Condicions i permisos | Condiciones y permisos>

- <Qué hace falta para cada acción.>
```
