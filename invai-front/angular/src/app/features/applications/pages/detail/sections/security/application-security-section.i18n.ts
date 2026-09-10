import {
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { KeyLabel } from '@models/table.model';

import { ApplicationSecurityResourceKind } from '../../../../forms/application-security-form.factory';

export const APPLICATION_SECURITY_SECTION_TITLE = $localize`Seguretat`;
export const APPLICATION_SECURITY_ROLES_TITLE = $localize`Rols de l'aplicació`;
export const APPLICATION_SECURITY_WEB_CONTEXTS_TITLE = $localize`Contextos web`;
export const APPLICATION_SECURITY_ENS_TITLE = $localize`Esquema Nacional de Seguretat`;
export const APPLICATION_SECURITY_DIMENSIONS_TITLE = $localize`Dimensions ENS`;
export const APPLICATION_SECURITY_RISKS_TITLE = $localize`Riscos de seguretat`;
export const APPLICATION_SECURITY_MEASURES_TITLE = $localize`Mesures de seguretat aplicades`;
export const APPLICATION_SECURITY_DOCUMENTATION_LABEL = $localize`Documentació`;
export const APPLICATION_SECURITY_DOCUMENTATION_ARIA_LABEL = $localize`Obre la documentació de seguretat`;
export const APPLICATION_SECURITY_DOCUMENTATION_PENDING = $localize`La documentació de seguretat encara no està disponible.`;
export const APPLICATION_SECURITY_PENDING_TITLE = $localize`Funcionalitat pendent`;
export const APPLICATION_SECURITY_ROLE_TEMPORARY_NOTE = $localize`La consulta de rols és temporal i de només lectura fins que estigui disponible el provider de Soffid.`;
export const APPLICATION_SECURITY_INCONSISTENT_TITLE = $localize`Dades ENS inconsistents`;
export const APPLICATION_SECURITY_INCONSISTENT_MESSAGE = $localize`S'ha trobat més d'una classificació ENS activa. L'edició d'aquest formulari està bloquejada fins que backend corregeixi les dades.`;
export const APPLICATION_SECURITY_ANCHOR_REQUIRED = $localize`Desa primer les dades generals de Seguretat per poder gestionar aquesta taula.`;
export const APPLICATION_SECURITY_ERROR_TITLE = $localize`Error`;
export const APPLICATION_SECURITY_LOAD_ERROR = $localize`No s'han pogut carregar totes les dades de Seguretat.`;
export const APPLICATION_SECURITY_SAVE_ERROR = $localize`No s'han pogut desar els canvis de Seguretat.`;
export const APPLICATION_SECURITY_RESOURCE_SAVE_SUCCESS = $localize`El registre s'ha desat correctament.`;
export const APPLICATION_SECURITY_RESOURCE_SAVE_ERROR = $localize`No s'ha pogut desar el registre.`;
export const APPLICATION_SECURITY_RESOURCE_DELETE_SUCCESS = $localize`El registre s'ha donat de baixa correctament.`;
export const APPLICATION_SECURITY_RESOURCE_DELETE_ERROR = $localize`No s'ha pogut donar de baixa el registre.`;
export const APPLICATION_SECURITY_SUCCESS_TITLE = $localize`Canvis desats`;
export const APPLICATION_SECURITY_REQUIRED_ERROR = $localize`Aquest camp és obligatori.`;
export const APPLICATION_SECURITY_EMPTY_VALUE = $localize`-`;
export const APPLICATION_SECURITY_DATE_FORMAT = $localize`:@@primengDateFormat:dd/mm/yy`;
export const APPLICATION_SECURITY_DATE_PLACEHOLDER = $localize`dd/mm/aaaa`;

export const APPLICATION_SECURITY_LABELS = {
  overallGrade: $localize`Grau d'adequació a l'ENS`,
  identityProvider: $localize`Proveïdor d'identitat`,
  ensSubject: $localize`Subjecció a l'ENS`,
  personalDataProcessing: $localize`Tractament de dades personals`,
  approvalDate: $localize`Data d'aprovació`,
  confidentiality: $localize`Confidencialitat`,
  integrity: $localize`Integritat`,
  traceability: $localize`Traçabilitat`,
  availability: $localize`Disponibilitat`,
  authenticity: $localize`Autenticitat`,
  observation: $localize`Observacions`,
  webContext: $localize`Context web`,
  field: $localize`Àmbit`,
  level: $localize`Nivell`,
  description: $localize`Descripció`,
  measureType: $localize`Tipus`,
  ensRequirement: $localize`Requisit ENS`,
};

export const APPLICATION_SECURITY_ROLE_COLUMNS: Partial<KeyLabel>[] = [
  {
    key: 'role',
    width: '25%',
    label: $localize`Codi`,
    sortBy: 'securityRole.name',
    minWidth: '12rem',
  },
  {
    key: 'system',
    width: '20%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Sistema`,
    sortBy: 'securityRole.system',
    minWidth: '12rem',
  },
  {
    key: 'description',
    width: '55%',
    wrap: true,
    maxWidth: '32rem',
    label: $localize`Descripció`,
    sortBy: 'securityRole.description',
    minWidth: '18rem',
  },
];
export const APPLICATION_SECURITY_WEB_CONTEXT_COLUMNS: Partial<KeyLabel>[] = [
  {
    key: 'webContext',
    width: '22%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Context web`,
    sortBy: 'webContext.name',
    minWidth: '13rem',
  },
  {
    key: 'field',
    width: '20%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Àmbit`,
    sortBy: 'field.name',
    minWidth: '11rem',
  },
  {
    key: 'observation',
    width: '58%',
    wrap: true,
    maxWidth: '32rem',
    label: $localize`Observació`,
    sortBy: 'observation',
    minWidth: '18rem',
  },
];
export const APPLICATION_SECURITY_RISK_COLUMNS: Partial<KeyLabel>[] = [
  { key: 'level', width: '15%', label: $localize`Nivell`, sortBy: 'level.name', minWidth: '9rem' },
  {
    key: 'description',
    width: '65%',
    wrap: true,
    maxWidth: '32rem',
    label: $localize`Descripció`,
    sortBy: 'description',
    minWidth: '18rem',
  },
  {
    key: 'field',
    width: '20%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Àmbit`,
    sortBy: 'field.name',
    minWidth: '11rem',
  },
];
export const APPLICATION_SECURITY_MEASURE_COLUMNS: Partial<KeyLabel>[] = [
  { key: 'type', width: '20%', label: $localize`Tipus`, sortBy: 'type.name', minWidth: '11rem' },
  {
    key: 'ensRequirement',
    width: '25%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Requisit ENS`,
    sortBy: 'ensRequirement.name',
    minWidth: '13rem',
  },
  {
    key: 'description',
    width: '55%',
    wrap: true,
    maxWidth: '32rem',
    label: $localize`Descripció`,
    sortBy: 'description',
    minWidth: '18rem',
  },
];

export const APPLICATION_SECURITY_TABLE_ACTIONS = {
  header: $localize`Accions`,
  ariaLabel: $localize`Obre les accions del registre`,
  view: $localize`Consulta`,
  edit: $localize`Edita`,
  delete: $localize`Dona de baixa`,
};

export const APPLICATION_SECURITY_ADD_ARIA_LABELS: Record<ApplicationSecurityResourceKind, string> =
  {
    'web-context': $localize`Afegeix un context web`,
    risk: $localize`Afegeix un risc de seguretat`,
    measure: $localize`Afegeix una mesura de seguretat`,
  };

export const APPLICATION_SECURITY_DIALOG_TITLES: Record<
  ApplicationSecurityResourceKind,
  Record<CrudEntityDialogMode, string>
> = {
  'web-context': {
    create: $localize`Afegeix un context web`,
    view: $localize`Consulta el context web`,
    edit: $localize`Edita el context web`,
  },
  risk: {
    create: $localize`Afegeix un risc de seguretat`,
    view: $localize`Consulta el risc de seguretat`,
    edit: $localize`Edita el risc de seguretat`,
  },
  measure: {
    create: $localize`Afegeix una mesura de seguretat`,
    view: $localize`Consulta la mesura de seguretat`,
    edit: $localize`Edita la mesura de seguretat`,
  },
};

export const APPLICATION_SECURITY_DIALOG_ARIA_LABELS: CrudEntityDialogAriaLabels = {
  accept: $localize`Accepta i tanca el diàleg`,
  add: $localize`Afegeix el registre`,
  cancel: $localize`Cancel·la els canvis`,
  close: $localize`Tanca el diàleg`,
  deactivate: $localize`Dona de baixa el registre`,
  edit: $localize`Edita el registre`,
  restore: $localize`Restaura el registre`,
  save: $localize`Desa el registre`,
};

export const APPLICATION_SECURITY_DELETE_DIALOG = {
  title: $localize`Dona de baixa el registre?`,
  message: $localize`El registre deixarà d'aparèixer a la taula de registres actius.`,
  cancelLabel: $localize`Cancel·la`,
  confirmLabel: $localize`Dona de baixa`,
  cancelAriaLabel: $localize`Cancel·la la baixa del registre`,
  confirmAriaLabel: $localize`Confirma la baixa del registre`,
};
