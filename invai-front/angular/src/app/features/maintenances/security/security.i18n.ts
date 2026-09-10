import {
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';

export const SECURITY_PANEL_COPY = {
  ensRequirement: {
    singular: $localize`:@@securityEnsRequirementSingular:requisit ENS`,
    plural: $localize`:@@securityEnsRequirementPlural:requisits ENS`,
    title: $localize`:@@securityEnsRequirementTitle:Requisits ENS`,
    description: $localize`:@@securityEnsRequirementDescription:Gestiona els requisits de l'Esquema Nacional de Seguretat.`,
  },
  identityProvider: {
    singular: $localize`:@@securityIdentityProviderSingular:proveïdor d'identitat`,
    plural: $localize`:@@securityIdentityProviderPlural:proveïdors d'identitat`,
    title: $localize`:@@securityIdentityProviderTitle:Proveïdors d'identitat`,
    description: $localize`:@@securityIdentityProviderDescription:Gestiona els proveïdors d'identitat disponibles.`,
  },
  personalDataProcessing: {
    singular: $localize`:@@securityPersonalDataProcessingSingular:tractament de dades personals`,
    plural: $localize`:@@securityPersonalDataProcessingPlural:tractaments de dades personals`,
    title: $localize`:@@securityPersonalDataProcessingTitle:Tractaments de dades personals`,
    description: $localize`:@@securityPersonalDataProcessingDescription:Gestiona les categories de tractament de dades personals.`,
  },
  securityMeasureType: {
    singular: $localize`:@@securityMeasureTypeSingular:tipus de mesura de seguretat`,
    plural: $localize`:@@securityMeasureTypePlural:tipus de mesura de seguretat`,
    title: $localize`:@@securityMeasureTypeTitle:Tipus de mesura de seguretat`,
    description: $localize`:@@securityMeasureTypeDescription:Gestiona els tipus de mesures de seguretat.`,
  },
  webContext: {
    singular: $localize`:@@securityWebContextSingular:context web`,
    plural: $localize`:@@securityWebContextPlural:contexts web`,
    title: $localize`:@@securityWebContextTitle:Contexts web`,
    description: $localize`:@@securityWebContextDescription:Gestiona els contexts web de seguretat.`,
  },
};

export const SECURITY_FILTER_LABELS = {
  name: $localize`:@@securityFilterName:Nom`,
  nameEs: $localize`:@@securityFilterNameEs:Nom en castellà`,
  status: $localize`:@@securityFilterStatus:Estat`,
};

export const SECURITY_QUICK_SEARCH_PLACEHOLDER =
  $localize`:@@securityQuickSearchPlaceholder:Cerca per nom`;
export const SECURITY_QUICK_SEARCH_ARIA_LABEL = (plural: string) =>
  $localize`:@@securityQuickSearchAria:Cerca ràpida de ${plural}:resource:`;
export const SECURITY_ADD_ARIA_LABEL = (singular: string) =>
  $localize`:@@securityAddAria:Afegeix un ${singular}:resource:`;
export const SECURITY_COLUMNS_INPUT_ID = (resource: string) =>
  `security-${resource}-columns`;
export const SECURITY_FILTERS_BUTTON_ARIA_LABEL = (plural: string) =>
  $localize`:@@securityFiltersButtonAria:Mostra o amaga els filtres de ${plural}:resource:`;
export const SECURITY_COLUMNS_BUTTON_ARIA_LABEL = (plural: string) =>
  $localize`:@@securityColumnsButtonAria:Selecciona les columnes de ${plural}:resource:`;
export const SECURITY_LOAD_ERROR_SUMMARY = $localize`:@@securityLoadErrorSummary:Error`;
export const SECURITY_LIST_LOAD_ERROR = (plural: string) =>
  $localize`:@@securityListLoadError:Error en carregar: ${plural}:resource:`;
export const SECURITY_ENTITY_LOAD_ERROR =
  $localize`:@@securityEntityLoadError:No s'ha pogut carregar el registre.`;
export const SECURITY_CREATED =
  $localize`:@@securityCreated:El registre s'ha afegit correctament.`;
export const SECURITY_UPDATED =
  $localize`:@@securityUpdated:El registre s'ha actualitzat correctament.`;
export const SECURITY_DEACTIVATED =
  $localize`:@@securityDeactivated:El registre s'ha donat de baixa correctament.`;
export const SECURITY_RESTORED =
  $localize`:@@securityRestored:El registre s'ha restaurat correctament.`;
export const SECURITY_SAVE_ERROR =
  $localize`:@@securitySaveError:No s'ha pogut desar el registre.`;
export const SECURITY_DEACTIVATE_ERROR =
  $localize`:@@securityDeactivateError:No s'ha pogut donar de baixa el registre.`;
export const SECURITY_RESTORE_ERROR =
  $localize`:@@securityRestoreError:No s'ha pogut restaurar el registre.`;
export const SECURITY_FORBIDDEN =
  $localize`:@@securityForbidden:No tens permisos per fer aquesta operació.`;

export const SECURITY_DEACTIVATE_DIALOG_TITLE =
  $localize`:@@securityDeactivateDialogTitle:Donar de baixa el registre?`;
export const SECURITY_DEACTIVATE_DIALOG_MESSAGE = (name: string) =>
  $localize`:@@securityDeactivateDialogMessage:Estàs a punt de donar de baixa «${name}:resourceName:».`;
export const SECURITY_DEACTIVATE_DIALOG_CANCEL_LABEL =
  $localize`:@@securityDeactivateDialogCancel:Cancel·lar`;
export const SECURITY_DEACTIVATE_DIALOG_CONFIRM_LABEL =
  $localize`:@@securityDeactivateDialogConfirm:Donar de baixa`;
export const SECURITY_DEACTIVATE_DIALOG_CANCEL_ARIA_LABEL =
  $localize`:@@securityDeactivateDialogCancelAria:Cancel·la la baixa del registre`;
export const SECURITY_DEACTIVATE_DIALOG_CONFIRM_ARIA_LABEL =
  $localize`:@@securityDeactivateDialogConfirmAria:Confirma la baixa del registre`;

export const SECURITY_DIALOG_TITLES = (singular: string): Record<CrudEntityDialogMode, string> => ({
  create: $localize`:@@securityDialogCreateTitle:Afegir ${singular}:resource:`,
  view: $localize`:@@securityDialogViewTitle:Consultar ${singular}:resource:`,
  edit: $localize`:@@securityDialogEditTitle:Editar ${singular}:resource:`,
});

export const SECURITY_DIALOG_ACTIONS = (singular: string): CrudEntityDialogAriaLabels => ({
  accept: $localize`:@@securityDialogAcceptAria:Accepta la consulta del ${singular}:resource:`,
  add: $localize`:@@securityDialogAddAria:Afegeix el ${singular}:resource:`,
  cancel: $localize`:@@securityDialogCancelAria:Cancel·la els canvis del ${singular}:resource:`,
  close: $localize`:@@securityDialogCloseAria:Tanca el formulari del ${singular}:resource:`,
  deactivate: $localize`:@@securityDialogDeactivateAria:Dona de baixa el ${singular}:resource:`,
  edit: $localize`:@@securityDialogEditAria:Edita el ${singular}:resource:`,
  restore: $localize`:@@securityDialogRestoreAria:Restaura el ${singular}:resource:`,
  save: $localize`:@@securityDialogSaveAria:Desa els canvis del ${singular}:resource:`,
});

export const SECURITY_DIALOG_LOADING =
  $localize`:@@securityDialogLoading:Carregant el registre`;
export const SECURITY_REQUIRED_ERROR = $localize`:@@securityRequiredError:Camp obligatori`;
export const SECURITY_NAME_MAX_LENGTH_ERROR =
  $localize`:@@securityNameMaxLengthError:El nom ha de tenir com a màxim 150 caràcters`;
export const SECURITY_NAME_ES_MAX_LENGTH_ERROR =
  $localize`:@@securityNameEsMaxLengthError:El nom en castellà ha de tenir com a màxim 100 caràcters`;

export const SECURITY_TABLE_ACTIONS_ARIA_LABEL =
  $localize`:@@securityTableActionsAria:Obrir les accions del registre`;
export const SECURITY_TABLE_VIEW_LABEL = $localize`:@@securityTableView:Consulta`;
export const SECURITY_TABLE_EDIT_LABEL = $localize`:@@securityTableEdit:Edita`;
export const SECURITY_TABLE_DEACTIVATE_LABEL =
  $localize`:@@securityTableDeactivate:Dona de baixa`;
