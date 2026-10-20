import {
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { KeyLabel } from '@models/table.model';

export const APPLICATION_INTEGRATIONS_SECTION_TITLE = $localize`:@@applicationIntegrationsTitle:Integracions`;
export const APPLICATION_INTEGRATIONS_OBSERVATIONS_LABEL = $localize`Observacions`;
export const APPLICATION_INTEGRATIONS_CONNECTIONS_TITLE = $localize`:@@applicationIntegrationsConnectionsTitle:Connexions`;
export const APPLICATION_INTEGRATIONS_ADD_ARIA_LABEL = $localize`:@@applicationIntegrationsAddConnection:Afegeix una connexió`;
export const APPLICATION_INTEGRATIONS_EMPTY_VALUE = '-';
export const APPLICATION_INTEGRATIONS_ERROR_TITLE = $localize`Error`;
export const APPLICATION_INTEGRATIONS_SUCCESS_TITLE = $localize`:@@applicationIntegrationsSuccessTitle:Canvis desats`;
export const APPLICATION_INTEGRATIONS_LOAD_ERROR = $localize`:@@applicationIntegrationsLoadError:No s'han pogut carregar les integracions.`;

export const APPLICATION_INTEGRATIONS_MESSAGES = {
  failed: $localize`:@@applicationIntegrationsFailed:No s'han pogut carregar les dades d'integració de l'aplicació. Torna-ho a provar.`,
  unavailable: $localize`:@@applicationIntegrationsUnavailable:No es pot identificar el registre d'integració d'aquesta aplicació. Torna a carregar les dades.`,
  forbidden: $localize`:@@applicationIntegrationsForbidden:El teu perfil no té permisos per fer aquesta operació d'integracions.`,
  deleted: $localize`:@@applicationIntegrationsDeleted:El registre d'integració està donat de baixa i no es pot modificar.`,
  retry: $localize`:@@applicationIntegrationsRetry:Torna a carregar les integracions`,
  infoTitle: $localize`:@@applicationIntegrationsInfoTitle:Informació`,
  anchorRequired: $localize`:@@applicationIntegrationsAnchorRequired:Desa les observacions de la pestanya per crear el registre d'integració abans d'afegir connexions.`,
  catalogsError: $localize`:@@applicationIntegrationsCatalogsError:No s'han pogut carregar les tecnologies o els sistemes externs. Torna a obrir el formulari.`,
  saveSuccess: $localize`:@@applicationIntegrationsConnectionSaved:La connexió s'ha desat correctament.`,
  saveError: $localize`:@@applicationIntegrationsConnectionSaveError:No s'ha pogut desar la connexió.`,
  deleteSuccess: $localize`:@@applicationIntegrationsConnectionDeleted:La connexió s'ha donat de baixa correctament.`,
  deleteError: $localize`:@@applicationIntegrationsConnectionDeleteError:No s'ha pogut donar de baixa la connexió.`,
};

export const APPLICATION_INTEGRATIONS_TABLE_ACTIONS = {
  header: $localize`Accions`,
  ariaLabel: $localize`Obre les accions del registre`,
  view: $localize`Consulta`,
  edit: $localize`Edita`,
  delete: $localize`Dona de baixa`,
};

export const APPLICATION_INTEGRATIONS_SYSTEM_KINDS = {
  application: $localize`:@@applicationIntegrationsKindApplication:Aplicació de l'inventari`,
  external: $localize`:@@applicationIntegrationsKindExternal:Sistema extern`,
};

export const APPLICATION_INTEGRATIONS_ROLES_TEXTS = {
  mismatch: $localize`:@@applicationIntegrationsRolesMismatch:No coincideixen`,
  mismatchDetail: $localize`:@@applicationIntegrationsRolesMismatchDetail:Els rols atorgats a Soffid no coincideixen amb els rols requerits.`,
  match: $localize`:@@applicationIntegrationsRolesMatch:Coincideixen`,
  unknown: $localize`:@@applicationIntegrationsRolesUnknown:No s'ha pogut comprovar`,
  grantedUnavailable: $localize`:@@applicationIntegrationsGrantedUnavailable:No disponible`,
};

export const APPLICATION_INTEGRATIONS_COLUMNS: KeyLabel[] = [
  {
    key: 'system',
    label: $localize`Sistema`,
    minWidth: '12rem',
    wrap: true,
    maxWidth: '24rem',
  },
  {
    key: 'technology',
    label: $localize`Tecnologia`,
    sortBy: 'technology.name',
    minWidth: '9rem',
    wrap: true,
    maxWidth: '24rem',
  },
  {
    key: 'username',
    label: $localize`:@@applicationIntegrationsUserColumn:Usuari d'integració`,
    sortBy: 'username',
    minWidth: '10rem',
  },
  {
    key: 'requiredRoles',
    label: $localize`:@@applicationIntegrationsRequiredRolesColumn:Rols requerits`,
    minWidth: '12rem',
    wrap: true,
    maxWidth: '24rem',
  },
  {
    key: 'grantedRoles',
    label: $localize`:@@applicationIntegrationsGrantedRolesColumn:Rols atorgats`,
    minWidth: '12rem',
    wrap: true,
    maxWidth: '24rem',
  },
  {
    key: 'rolesMismatch',
    label: $localize`:@@applicationIntegrationsRolesMatchColumn:Coincidència de rols`,
    minWidth: '10rem',
  },
];

export const APPLICATION_INTEGRATIONS_DELETE_DIALOG = {
  title: $localize`:@@applicationIntegrationsDeleteTitle:Dona de baixa la connexió?`,
  message: $localize`:@@applicationIntegrationsDeleteMessage:La connexió deixarà d'aparèixer a la taula de connexions actives.`,
  cancelLabel: $localize`Cancel·la`,
  confirmLabel: $localize`Dona de baixa`,
  cancelAriaLabel: $localize`:@@applicationIntegrationsDeleteCancelAria:Cancel·la la baixa de la connexió`,
  confirmAriaLabel: $localize`:@@applicationIntegrationsDeleteConfirmAria:Confirma la baixa de la connexió`,
};

export const APPLICATION_INTEGRATION_DIALOG_TITLES: Record<CrudEntityDialogMode, string> = {
  create: $localize`:@@applicationIntegrationDialogCreateTitle:Afegir connexió`,
  view: $localize`:@@applicationIntegrationDialogViewTitle:Consultar connexió`,
  edit: $localize`:@@applicationIntegrationDialogEditTitle:Editar connexió`,
};

export const APPLICATION_INTEGRATION_DIALOG_LABELS = {
  systemKind: $localize`:@@applicationIntegrationDialogSystemKind:Tipus de sistema`,
  application: $localize`:@@applicationIntegrationDialogApplication:Aplicació`,
  externalSystem: $localize`:@@applicationIntegrationDialogExternalSystem:Sistema extern`,
  technology: $localize`Tecnologia`,
  user: $localize`:@@applicationIntegrationDialogUser:Usuari d'integració`,
  requiredRoles: $localize`:@@applicationIntegrationsRequiredRolesColumn:Rols requerits`,
  grantedRoles: $localize`:@@applicationIntegrationsGrantedRolesColumn:Rols atorgats`,
  rolesMismatch: $localize`:@@applicationIntegrationsRolesMatchColumn:Coincidència de rols`,
};

export const APPLICATION_INTEGRATION_DIALOG_MESSAGES = {
  required: $localize`:@@applicationIntegrationDialogRequired:Camp obligatori`,
  rolesRequired: $localize`:@@applicationIntegrationDialogRolesRequired:Selecciona almenys un rol requerit.`,
  userRequired: $localize`:@@applicationIntegrationDialogUserRequired:Selecciona un usuari de Soffid.`,
  userInvalid: $localize`:@@applicationIntegrationDialogUserInvalid:Selecciona un usuari de la llista de Soffid que tingui codi d'usuari.`,
  applicationPlaceholder: $localize`:@@applicationIntegrationDialogApplicationPlaceholder:Cerca per nom o codi`,
  applicationInstruction: $localize`:@@applicationIntegrationDialogApplicationInstruction:Obre la llista i escriu per cercar aplicacions de l'inventari.`,
  applicationsSearching: $localize`:@@applicationIntegrationDialogApplicationsSearching:Cercant aplicacions…`,
  applicationsEmpty: $localize`:@@applicationIntegrationDialogApplicationsEmpty:No s'han trobat aplicacions.`,
  applicationsError: $localize`:@@applicationIntegrationDialogApplicationsError:No s'han pogut cercar aplicacions. Torna-ho a provar.`,
  rolesPlaceholder: $localize`:@@applicationIntegrationDialogRolesPlaceholder:Cerca rols de Soffid`,
  rolesInstruction: $localize`:@@applicationIntegrationDialogRolesInstruction:Obre la llista i escriu el nom del rol, per exemple amb el prefix INV_.`,
  rolesSearching: $localize`:@@applicationIntegrationDialogRolesSearching:Cercant rols a Soffid…`,
  rolesEmpty: $localize`:@@applicationIntegrationDialogRolesEmpty:No s'han trobat rols a Soffid.`,
  rolesError: $localize`:@@applicationIntegrationDialogRolesError:No s'ha pogut consultar Soffid. Torna-ho a provar.`,
  catalogLoading: $localize`:@@applicationIntegrationDialogCatalogLoading:Carregant opcions…`,
};

export const APPLICATION_INTEGRATION_DIALOG_ARIA_LABELS: CrudEntityDialogAriaLabels = {
  accept: $localize`:@@applicationIntegrationDialogAcceptAria:Accepta la consulta de la connexió`,
  add: $localize`:@@applicationIntegrationDialogAddAria:Afegeix la connexió`,
  cancel: $localize`:@@applicationIntegrationDialogCancelAria:Cancel·la els canvis de la connexió`,
  close: $localize`:@@applicationIntegrationDialogCloseAria:Tanca el formulari de la connexió`,
  deactivate: $localize`:@@applicationIntegrationDialogDeactivateAria:Dona de baixa la connexió`,
  edit: $localize`:@@applicationIntegrationDialogEditAria:Edita la connexió`,
  restore: $localize`:@@applicationIntegrationDialogRestoreAria:Restaura la connexió`,
  save: $localize`:@@applicationIntegrationDialogSaveAria:Desa els canvis de la connexió`,
};
