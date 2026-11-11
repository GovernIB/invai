### Sistemes i DB

#### Objectiu

Mantenir els servidors d’aplicacions, les bases de dades, els servidors físics que els allotgen, els proveïdors de bases de dades i els entorns. Aquests registres es poden seleccionar en les relacions d’infraestructura de les aplicacions.

#### Com s'hi accedeix

Obriu **Manteniments** → **Sistemes i DB**. Premeu el títol d’un apartat per desplegar-lo; amb el teclat, utilitzau <kbd>Tab</kbd> i <kbd>Intro</kbd>.

![Apartats del manteniment de Sistemes i DB](imgs/systems.webp)

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

##### Servidors d'aplicacions

- **Servidor**
- **Instància**
- **Versió**
- **Estat**

##### Bases de dades

- **Servidor de BD**
- **Servei / SID**
- **Proveïdor**
- **Estat**

##### Servidors físics

- **Servidor**
- **Entorn**
- **Estat**

##### Servidors de bases de dades

- **Servidor**
- **Entorn**
- **Estat**

##### Proveïdors de bases de dades

- **Proveïdor**
- **Port per defecte**
- **Estat**

##### Entorns

- **Codi**
- **Nom**
- **Nom en castellà**
- **Estat**

#### Taula de resultats

Les capçaleres ordenables canvien l’ordre; els controls de paginació permeten recórrer la llista. Les columnes disponibles són:

##### Servidors d'aplicacions

- **Servidor**
- **Entorn**
- **Instància**
- **Port**
- **Versió**
- **Observacions**
- **Estat**

##### Bases de dades

- **Servidor de BD**
- **Entorn**
- **Servei / SID**
- **Port**
- **Proveïdor**
- **Observacions**
- **Estat**

##### Servidors físics

- **Servidor**
- **Entorn**
- **Estat**

##### Servidors de bases de dades

- **Servidor**
- **Entorn**
- **Estat**

##### Proveïdors de bases de dades

- **Proveïdor**
- **Port per defecte**
- **Estat**

##### Entorns

- **Codi**
- **Nom**
- **Nom en castellà**
- **Estat**

La columna d’estat es mostra per defecte quan consultau tots els estats i s’oculta quan n’aplicau un.

#### Formulari

En consulta, les dades es poden llegir. Per modificar-les, activau **Editar**.

##### Servidors d'aplicacions

Obligatoris:

- **Servidor**.
- **Instància**: màxim 50 caràcters.
- **Port**: de l’1 al 65535.
- **Versió**: màxim 20 caràcters.

Opcionals:

- **Observacions**

![Formulari d’alta de Servidors d'aplicacions](imgs/systems-servers.webp)

##### Bases de dades

Obligatoris:

- **Servidor de BD**.
- **Servei / SID**: màxim 255 caràcters.
- **Port**: de l’1 al 65535.
- **Proveïdor**.

Opcionals:

- **Observacions**

En seleccionar un proveïdor, el port es completa amb el seu port per defecte; revisau-lo abans de desar.

![Formulari d’alta de Bases de dades](imgs/systems-databases.webp)

##### Servidors físics

Obligatoris:

- **Servidor**: màxim 255 caràcters.
- **Entorn**.

![Formulari d’alta de Servidors físics](imgs/systems-physical-servers.webp)

##### Servidors de bases de dades

Obligatoris:

- **Servidor**: màxim 255 caràcters.
- **Entorn**.

![Formulari d’alta de Servidors de bases de dades](imgs/systems-database-servers.webp)

##### Proveïdors de bases de dades

Obligatoris:

- **Proveïdor**: màxim 100 caràcters.
- **Port per defecte**: de l’1 al 65535.

![Formulari d’alta de Proveïdors de bases de dades](imgs/systems-database-vendors.webp)

##### Entorns

Obligatoris:

- **Codi**: màxim 50 caràcters.
- **Nom**: màxim 100 caràcters.
- **Nom en castellà**: màxim 100 caràcters.

![Formulari d’alta de Entorns](imgs/systems-environments.webp)

#### Desar

**Afegir** crea el registre; **Desar** desa una edició. Corregiu els camps assenyalats si hi ha errors. En consulta, **Acceptar** tanca el formulari. **Cancel·lar** abandona l’alta o recupera la consulta en cancel·lar una edició. Si tancau una edició modificada, podeu desar o descartar els canvis.

#### Condicions i permisos

Els ports han de ser nombres enters. Les observacions tenen un límit de 4000 bytes de text; els caràcters especials poden ocupar més d’un byte i el formulari avisa si el superau.

Les operacions depenen dels permisos de la sessió i de l’estat del registre. Les opcions dels desplegables provenen dels catàlegs; si falta una opció necessària, revisau el seu manteniment. Un error en desar no confirma l’operació: revisau el missatge i torneu-ho a provar quan s’hagi resolt.
