### Seguretat

#### Objectiu

Mantenir els contexts web, proveïdors d’identitat, tractaments de dades personals, tipus de mesura de seguretat i requisits ENS disponibles per descriure la seguretat de les aplicacions.

#### Com s'hi accedeix

Obriu **Manteniments** → **Seguretat**. Premeu el títol d’un apartat per desplegar-lo; amb el teclat, utilitzau <kbd>Tab</kbd> i <kbd>Intro</kbd>.

![Apartats del manteniment de Seguretat](imgs/security.webp)

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

##### Contexts web

- **Nom**
- **Nom en castellà**
- **Estat**

##### Proveïdors d'identitat

- **Nom**
- **Estat**

##### Tractaments de dades personals

- **Nom**
- **Nom en castellà**
- **Estat**

##### Tipus de mesura de seguretat

- **Nom**
- **Nom en castellà**
- **Estat**

##### Requisits ENS

- **Nom**
- **Nom en castellà**
- **Estat**

#### Taula de resultats

Les capçaleres ordenables canvien l’ordre; els controls de paginació permeten recórrer la llista. Les columnes disponibles són:

##### Contexts web

- **Nom**
- **Nom en castellà**
- **Estat**

##### Proveïdors d'identitat

- **Nom**
- **Estat**

##### Tractaments de dades personals

- **Nom**
- **Nom en castellà**
- **Estat**

##### Tipus de mesura de seguretat

- **Nom**
- **Nom en castellà**
- **Estat**

##### Requisits ENS

- **Nom**
- **Nom en castellà**
- **Estat**

La columna d’estat es mostra per defecte quan consultau tots els estats i s’oculta quan n’aplicau un.

#### Formulari

En consulta, les dades es poden llegir. Per modificar-les, activau **Editar**.

##### Contexts web

Obligatoris:

- **Nom**: màxim 150 caràcters.
- **Nom en castellà**: màxim 100 caràcters.

![Formulari d’alta de Contexts web](imgs/security-web-contexts.webp)

##### Proveïdors d'identitat

Obligatoris:

- **Nom**: màxim 150 caràcters.

![Formulari d’alta de Proveïdors d'identitat](imgs/security-identity-providers.webp)

##### Tractaments de dades personals

Obligatoris:

- **Nom**: màxim 150 caràcters.
- **Nom en castellà**: màxim 100 caràcters.

![Formulari d’alta de Tractaments de dades personals](imgs/security-personal-data-processing.webp)

##### Tipus de mesura de seguretat

Obligatoris:

- **Nom**: màxim 150 caràcters.
- **Nom en castellà**: màxim 100 caràcters.

![Formulari d’alta de Tipus de mesura de seguretat](imgs/security-security-measure-types.webp)

##### Requisits ENS

Obligatoris:

- **Nom**: màxim 150 caràcters.
- **Nom en castellà**: màxim 100 caràcters.

![Formulari d’alta de Requisits ENS](imgs/security-ens-requirements.webp)

#### Desar

**Afegir** crea el registre; **Desar** desa una edició. Corregiu els camps assenyalats si hi ha errors. En consulta, **Acceptar** tanca el formulari. **Cancel·lar** abandona l’alta o recupera la consulta en cancel·lar una edició. Si tancau una edició modificada, podeu desar o descartar els canvis.

#### Condicions i permisos

Les operacions depenen dels permisos de la sessió i de l’estat del registre. Les opcions dels desplegables provenen dels catàlegs; si falta una opció necessària, revisau el seu manteniment. Un error en desar no confirma l’operació: revisau el missatge i torneu-ho a provar quan s’hagi resolt.
