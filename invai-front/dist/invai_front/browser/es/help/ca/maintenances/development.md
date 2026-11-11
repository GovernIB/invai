### Desenvolupament

#### Objectiu

Mantenir els rols de proveïdor, les tecnologies i les capes que s’utilitzen en la configuració de desenvolupament de les aplicacions. Cada tecnologia pertany a una capa.

#### Com s'hi accedeix

Obriu **Manteniments** → **Desenvolupament**. Premeu el títol d’un apartat per desplegar-lo; amb el teclat, utilitzau <kbd>Tab</kbd> i <kbd>Intro</kbd>.

![Apartats del manteniment de Desenvolupament](imgs/development.webp)

#### Accions disponibles

- **+** obre l’alta del catàleg desplegat.
- L’embut mostra o amaga els filtres.
- El selector de columnes permet triar les columnes visibles.
- La creu de la cerca ràpida neteja el text.
- El menú de tres punts d’una fila permet consultar, editar o donar de baixa; la baixa demana confirmació.
- Doble clic o <kbd>Intro</kbd> sobre una fila obre la consulta.
- En un registre inactiu, la restauració permet recuperar-lo.
- El botó d’ajuda del formulari i <kbd>Ctrl</kbd> + <kbd>K</kbd> obren aquest tema.

#### Cerca i filtres

La cerca ràpida filtra els registres. Per combinar criteris, obriu l’embut i premeu **Cerca**. **Neteja** restableix els filtres. **Estat** parteix d’**Actiu**; podeu seleccionar **Inactiu** o netejar-lo per consultar tots els estats.

##### Rols de proveïdor

- **Nom**
- **Nom en castellà**
- **Estat**

##### Tecnologies

- **Nom**
- **Capa**
- **Estat**

##### Capes

- **Nom**
- **Estat**

#### Taula de resultats

Les capçaleres ordenables canvien l’ordre; els controls de paginació permeten recórrer la llista. Les columnes disponibles són:

##### Rols de proveïdor

- **Nom**
- **Nom en castellà**
- **Estat**

##### Tecnologies

- **Nom**
- **Capa**
- **Estat**

##### Capes

- **Nom**
- **Estat**

La columna d’estat es mostra per defecte quan consultau tots els estats i s’oculta quan n’aplicau un.

#### Formulari

En consulta, les dades es poden llegir. Per modificar-les, activau **Editar**.

##### Rols de proveïdor

Obligatoris:

- **Nom**: màxim 100 caràcters.

Opcionals:

- **Nom en castellà**

El nom en castellà admet fins a 100 caràcters.

![Formulari d’alta de Rols de proveïdor](imgs/development-provider-roles.webp)

##### Tecnologies

Obligatoris:

- **Nom**: màxim 100 caràcters.
- **Capa**.

![Formulari d’alta de Tecnologies](imgs/development-technologies.webp)

##### Capes

Obligatoris:

- **Nom**: màxim 100 caràcters.

![Formulari d’alta de Capes](imgs/development-layers.webp)

#### Desar

**Afegir** crea el registre; **Desar** desa una edició. Corregiu els camps assenyalats si hi ha errors. En consulta, **Acceptar** tanca el formulari. **Cancel·lar** abandona l’alta o recupera la consulta en cancel·lar una edició. Si tancau una edició modificada, podeu desar o descartar els canvis.

#### Condicions i permisos

Les operacions depenen dels permisos de la sessió i de l’estat del registre. Les opcions dels desplegables provenen dels catàlegs; si falta una opció necessària, revisau el seu manteniment. Un error en desar no confirma l’operació: revisau el missatge i torneu-ho a provar quan s’hagi resolt.
