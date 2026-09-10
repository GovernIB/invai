import { KeyLabel } from '@models/table.model';

export const APPLICATION_DEVELOPMENT_SECTION_TITLE = $localize`:@@applicationDevelopmentSectionTitle:Configuració del desenvolupament`;

export const APPLICATION_DEVELOPMENT_LABELS = {
  environment: $localize`Entorn`,
  modality: $localize`Modalitat`,
  code: $localize`Codi font`,
  standardAdaption: $localize`Adequació estàndards GOIB`,
  revisionDate: $localize`Darrera revisió d'estàndards`,
  observation: $localize`Observacions (Opcional)`,
};

export const APPLICATION_DEVELOPMENT_PROVIDER_TITLE = $localize`Proveïdor`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGY_TITLE = $localize`Tecnologia`;
export const APPLICATION_DEVELOPMENT_ADD_PROVIDER_ARIA_LABEL = $localize`Afegeix un proveïdor`;
export const APPLICATION_DEVELOPMENT_ADD_TECHNOLOGY_ARIA_LABEL = $localize`Afegeix una tecnologia`;
export const APPLICATION_DEVELOPMENT_DOCUMENTATION_LABEL = $localize`Documentació`;
export const APPLICATION_DEVELOPMENT_DOCUMENTATION_ARIA_LABEL = $localize`Obrir la documentació del desenvolupament`;
export const APPLICATION_DEVELOPMENT_SOURCE_CODE_ARIA_LABEL = $localize`Obrir el repositori de codi font`;

export const APPLICATION_DEVELOPMENT_PROVIDER_COLUMNS: KeyLabel[] = [
  {
    key: 'companyName',
    width: '40%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Raó social`,
    sortBy: 'companyName',
    minWidth: '12rem',
  },
  {
    key: 'role',
    width: '30%',
    label: $localize`Rol`,
    sortBy: 'role.name',
    minWidth: '10rem',
  },
  {
    key: 'startDate',
    width: '15%',
    label: $localize`Data inici`,
    sortBy: 'startDate',
    minWidth: '9rem',
  },
  {
    key: 'expireDate',
    width: '15%',
    label: $localize`Data fi`,
    sortBy: 'expireDate',
    minWidth: '9rem',
  },
];

export const APPLICATION_DEVELOPMENT_TECHNOLOGY_COLUMNS: KeyLabel[] = [
  {
    key: 'layer',
    width: '20%',
    label: $localize`Capa`,
    sortBy: 'layer.name',
    minWidth: '10rem',
  },
  {
    key: 'technology',
    width: '40%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Tecnologia`,
    sortBy: 'technology.name',
    minWidth: '12rem',
  },
  {
    key: 'version',
    width: '20%',
    label: $localize`Versió`,
    sortBy: 'version',
    minWidth: '9rem',
  },
  {
    key: 'architecture',
    width: '20%',
    label: $localize`Arquitectura`,
    sortBy: 'architecture',
    minWidth: '11rem',
  },
];

export const APPLICATION_DEVELOPMENT_REQUIRED_ERROR = $localize`Camp obligatori`;
export const APPLICATION_DEVELOPMENT_URL_ERROR = $localize`Introdueix una URL HTTP o HTTPS vàlida`;
export const APPLICATION_DEVELOPMENT_CODE_MAX_LENGTH_ERROR = $localize`La URL ha de tenir com a màxim 1000 caràcters`;

export const APPLICATION_DEVELOPMENT_PENDING_TITLE = $localize`Informació`;
export const APPLICATION_DEVELOPMENT_DOCUMENTATION_PENDING = $localize`La documentació del desenvolupament encara no està implementada.`;

export const APPLICATION_DEVELOPMENT_ERROR_TITLE = $localize`Error`;
export const APPLICATION_DEVELOPMENT_LOAD_ERROR = $localize`No s'han pogut carregar les dades de desenvolupament.`;
export const APPLICATION_DEVELOPMENT_PROVIDERS_LOAD_ERROR = $localize`No s'han pogut carregar els proveïdors.`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGIES_LOAD_ERROR = $localize`No s'han pogut carregar les tecnologies.`;
export const APPLICATION_DEVELOPMENT_ENVIRONMENTS_LOAD_ERROR = $localize`No s'han pogut carregar els entorns.`;
export const APPLICATION_DEVELOPMENT_ROLES_LOAD_ERROR = $localize`No s'han pogut carregar els rols de proveïdor.`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGY_OPTIONS_LOAD_ERROR = $localize`No s'ha pogut carregar el catàleg de tecnologies.`;
export const APPLICATION_DEVELOPMENT_MODALITIES_LOAD_ERROR = $localize`No s'ha pogut carregar el catàleg de modalitats. El valor actual es manté visible.`;
export const APPLICATION_DEVELOPMENT_STANDARD_ADAPTIONS_LOAD_ERROR = $localize`No s'ha pogut carregar el catàleg d'adequació a estàndards. El valor actual es manté visible.`;
export const APPLICATION_DEVELOPMENT_PROVIDER_CREATE_SUCCESS = $localize`S'ha afegit el proveïdor correctament.`;
export const APPLICATION_DEVELOPMENT_PROVIDER_CREATE_ERROR = $localize`No s'ha pogut afegir el proveïdor.`;
export const APPLICATION_DEVELOPMENT_PROVIDER_UPDATE_SUCCESS = $localize`S'ha actualitzat el proveïdor correctament.`;
export const APPLICATION_DEVELOPMENT_PROVIDER_UPDATE_ERROR = $localize`No s'ha pogut actualitzar el proveïdor.`;
export const APPLICATION_DEVELOPMENT_PROVIDER_DELETE_SUCCESS = $localize`S'ha donat de baixa el proveïdor correctament.`;
export const APPLICATION_DEVELOPMENT_PROVIDER_DELETE_ERROR = $localize`No s'ha pogut donar de baixa el proveïdor.`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGY_CREATE_SUCCESS = $localize`S'ha afegit la tecnologia correctament.`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGY_CREATE_ERROR = $localize`No s'ha pogut afegir la tecnologia.`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGY_UPDATE_SUCCESS = $localize`S'ha actualitzat la tecnologia correctament.`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGY_UPDATE_ERROR = $localize`No s'ha pogut actualitzar la tecnologia.`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_SUCCESS = $localize`S'ha donat de baixa la tecnologia correctament.`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_ERROR = $localize`No s'ha pogut donar de baixa la tecnologia.`;
export const APPLICATION_DEVELOPMENT_DELETE_DIALOG_CANCEL_LABEL = $localize`Cancel·la`;
export const APPLICATION_DEVELOPMENT_DELETE_DIALOG_CONFIRM_LABEL = $localize`Dona de baixa`;
export const APPLICATION_DEVELOPMENT_PROVIDER_DELETE_DIALOG_TITLE = $localize`Dona de baixa el proveïdor`;
export const APPLICATION_DEVELOPMENT_PROVIDER_DELETE_DIALOG_MESSAGE = (companyName: string) =>
  $localize`Vols donar de baixa el proveïdor ${companyName}?`;
export const APPLICATION_DEVELOPMENT_PROVIDER_DELETE_DIALOG_CANCEL_ARIA_LABEL = $localize`Cancel·la la baixa del proveïdor`;
export const APPLICATION_DEVELOPMENT_PROVIDER_DELETE_DIALOG_CONFIRM_ARIA_LABEL = $localize`Confirma la baixa del proveïdor`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_DIALOG_TITLE = $localize`Dona de baixa la tecnologia`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_DIALOG_MESSAGE = (technologyName: string) =>
  $localize`Vols donar de baixa la tecnologia ${technologyName}?`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_DIALOG_CANCEL_ARIA_LABEL = $localize`Cancel·la la baixa de la tecnologia`;
export const APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_DIALOG_CONFIRM_ARIA_LABEL = $localize`Confirma la baixa de la tecnologia`;
export const APPLICATION_DEVELOPMENT_DATE_FORMAT = 'dd/mm/yy';
export const APPLICATION_DEVELOPMENT_DATE_PLACEHOLDER = $localize`dd/mm/aaaa`;
