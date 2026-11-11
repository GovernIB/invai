# Centro de ayuda: prueba con lector de pantalla

Guion para la revisión manual de la fase 3 con NVDA (Windows) o Narrador. Lo automático ya está
comprobado (e2e de `help-center.spec.ts`, escaneo Axe, árbol de accesibilidad, reflujo a 320 px y
espaciado de texto); este guion cubre lo que solo se oye.

Preparación: `npm run local` (catalán), Chrome o Edge, NVDA en modo navegación por defecto. Entre
corchetes, lo que debería decir el lector; la redacción exacta varía según el lector y su
configuración.

## 1. Abrir y cerrar

1. En la lista de aplicaciones, pulsa <kbd>Ctrl</kbd> + <kbd>K</kbd>.
   - [«Centre d'ajuda, diàleg», «Tema obert: Llista d'aplicacions», «Cerca en tota l'ajuda,
     quadre combinat»].
   - El foco queda en el buscador. Con <kbd>Mayús</kbd> + <kbd>Tab</kbd> llegas a «Tanca el centre
     d'ajuda» y «Maximitza».
2. Pulsa <kbd>Esc</kbd>.
   - El diálogo se cierra y el foco vuelve a donde estaba.
3. Abre el menú de usuario, ve a **Obrir ajuda** con <kbd>Tab</kbd>.
   - [«Obrir ajuda, botó»; si el lector anuncia atajos, «Control+K»].

## 2. Buscar

1. Con la ayuda abierta, escribe `menu`.
   - [«7 temes trobats»] y la primera opción: [«Capçalera i menú d'usuari, Primers passos, 1 de 7»].
   - No debe decir «Tema seleccionat» mientras escribes.
2. Baja con <kbd>↓</kbd>: cada opción se lee con su grupo
   («Responsables, Aplicacions, Fitxa d'aplicació»).
3. Escribe algo que no exista (`zzzz`).
   - [«No s'han trobat resultats»].
4. Borra, escribe `ajuda` y pulsa <kbd>Intro</kbd> en la primera opción.
   - [«Com fer servir l'ajuda, encapçalament nivell 3»]: el foco pasa al título del tema.
5. Pulsa <kbd>Esc</kbd> con las sugerencias abiertas: solo se cierran las sugerencias; un segundo
   <kbd>Esc</kbd> cierra la ayuda.

## 3. Índice

1. Desde el buscador, <kbd>Tab</kbd> hasta el índice.
   - [«Temes d'ajuda, navegació», «Primers passos, botó, expandit»].
2. <kbd>Intro</kbd> sobre «Primers passos».
   - [«contret» / «replegat»]; los temas del grupo dejan de leerse. Otro <kbd>Intro</kbd> lo
     vuelve a desplegar.
3. <kbd>Tab</kbd> hasta un tema e <kbd>Intro</kbd>.
   - El foco va al título del tema. Vuelve con <kbd>Mayús</kbd> + <kbd>Tab</kbd> al índice: el tema
     abierto se anuncia como [«actual»].
4. En modo navegación, <kbd>L</kbd> e <kbd>I</kbd> recorren las listas y sus elementos: deben leerse
   como listas anidadas, con su número de elementos.

## 4. Contenido de un tema

1. Abre «Capçalera i menú d'usuari» y recórrelo con <kbd>H</kbd> (encabezados).
   - Nivel 3 para el título, nivel 4 para los apartados, nivel 5 para los subapartados.
2. Las imágenes se anuncian con su texto alternativo.
3. Las teclas (<kbd>Ctrl</kbd>, <kbd>K</kbd>…) se leen como texto normal.
4. En «Alta d'una aplicació», activa el enlace «Desar».
   - El foco salta al apartado «Desar» del mismo tema.
5. Activa el enlace «Llista d'aplicacions».
   - Se abre ese tema dentro de la ayuda; la página de detrás no cambia.

## 5. Ayuda contextual

1. Abre una aplicación (pestaña General) y pulsa <kbd>Ctrl</kbd> + <kbd>K</kbd>.
   - [«Tema obert: General»].
2. Cierra, ve a la pestaña Desenvolupament y vuelve a abrir.
   - [«Tema obert: Desenvolupament»]. Repite con Accessibilitat y Seguretat: cada una anuncia su tema.
3. Cierra, ve a Dades y vuelve a abrir.
   - [«Tema obert: Dades»]. Repite con Integracions y Documentació: cada una anuncia su tema.
4. En Documentació, abre la ficha de un adjunto y vuelve a abrir la ayuda.
   - [«Tema obert: Detall d'un document»]. El foco queda en el buscador.
5. En el tema Documentació, activa «Detall d'un document».
   - Se abre ese tema dentro de la ayuda y el foco pasa a su encabezado, sin cambiar la pantalla.

## Mantenimientos General y Responsables

En Manteniments, repetir la apertura contextual desde General y Responsables y desde el botón
de ayuda de los siete formularios de categorías, sistemas de información, ámbitos, comisiones,
personas, autorizaciones y empresas. Comprobar el anuncio del tema, el foco inicial en el buscador
y su devolución al botón del formulario al cerrar. Estas comprobaciones humanas siguen pendientes.

## Mantenimientos adicionales

La fase 5.6 añade Sistemas, Desarrollo, Accesibilidad, Seguridad, Integraciones, Documentación y
el detalle de una categoría documental. Repetir el anuncio del tema y el retorno del foco desde
las seis pestañas, sus dieciocho apartados CRUD y el formulario de subcategoría del detalle.
Comprobar también la lectura de las listas de campos y de los nombres de permisos del nuevo tema.
El paso humano con NVDA o Narrador continúa pendiente.

## Fallos ya conocidos (no hace falta anotarlos)

- Al abrir la ayuda en un tema que aún no se ha cargado la primera vez, el lector puede no leer
  «Tema obert»: la descripción se rellena cuando llega el índice.

Anota lo que no coincida en «Paso manual con NVDA o Narrador» de [PLAN.md](PLAN.md).
