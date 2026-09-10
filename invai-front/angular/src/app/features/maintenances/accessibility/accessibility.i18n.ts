import {
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';

export const ACCESSIBILITY_PANEL_COPY = {
  classificationSegment: {
    singular: $localize`:@@accessibilityClassificationSegmentSingular:segment de classificació`,
    plural: $localize`:@@accessibilityClassificationSegmentPlural:segments de classificació`,
    title: $localize`:@@accessibilityClassificationSegmentTitle:Segments de classificació`,
    description: $localize`:@@accessibilityClassificationSegmentDescription:Gestiona els segments de classificació d'accessibilitat.`,
  },
  complianceSituation: {
    singular: $localize`:@@accessibilityComplianceSituationSingular:situació de compliment`,
    plural: $localize`:@@accessibilityComplianceSituationPlural:situacions de compliment`,
    title: $localize`:@@accessibilityComplianceSituationTitle:Situacions de compliment`,
    description: $localize`:@@accessibilityComplianceSituationDescription:Gestiona les situacions de compliment d'accessibilitat.`,
  },
};

export const ACCESSIBILITY_FILTER_LABELS = {
  name: $localize`:@@accessibilityFilterName:Nom`,
  nameEs: $localize`:@@accessibilityFilterNameEs:Nom en castellà`,
  status: $localize`:@@accessibilityFilterStatus:Estat`,
};

export const ACCESSIBILITY_QUICK_SEARCH_PLACEHOLDER = $localize`:@@accessibilityQuickSearchPlaceholder:Cerca per nom`;
export const ACCESSIBILITY_QUICK_SEARCH_ARIA_LABEL = (plural: string) =>
  $localize`:@@accessibilityQuickSearchAria:Cerca ràpida de ${plural}:resource:`;
export const ACCESSIBILITY_ADD_ARIA_LABEL = (singular: string) =>
  $localize`:@@accessibilityAddAria:Afegeix: ${singular}:resource:`;
export const ACCESSIBILITY_COLUMNS_INPUT_ID = (resource: string) =>
  `accessibility-${resource}-columns`;
export const ACCESSIBILITY_FILTERS_BUTTON_ARIA_LABEL = (plural: string) =>
  $localize`:@@accessibilityFiltersButtonAria:Mostra o amaga els filtres de ${plural}:resource:`;
export const ACCESSIBILITY_COLUMNS_BUTTON_ARIA_LABEL = (plural: string) =>
  $localize`:@@accessibilityColumnsButtonAria:Selecciona les columnes de ${plural}:resource:`;
export const ACCESSIBILITY_LOAD_ERROR_SUMMARY = $localize`:@@accessibilityLoadErrorSummary:Error`;
export const ACCESSIBILITY_LIST_LOAD_ERROR = (plural: string) =>
  $localize`:@@accessibilityListLoadError:Error en carregar: ${plural}:resource:`;
export const ACCESSIBILITY_ENTITY_LOAD_ERROR = $localize`:@@accessibilityEntityLoadError:No s'ha pogut carregar el registre.`;
export const ACCESSIBILITY_CREATED = $localize`:@@accessibilityCreated:El registre s'ha afegit correctament.`;
export const ACCESSIBILITY_UPDATED = $localize`:@@accessibilityUpdated:El registre s'ha actualitzat correctament.`;
export const ACCESSIBILITY_DEACTIVATED = $localize`:@@accessibilityDeactivated:El registre s'ha donat de baixa correctament.`;
export const ACCESSIBILITY_RESTORED = $localize`:@@accessibilityRestored:El registre s'ha restaurat correctament.`;
export const ACCESSIBILITY_SAVE_ERROR = $localize`:@@accessibilitySaveError:No s'ha pogut desar el registre.`;
export const ACCESSIBILITY_DEACTIVATE_ERROR = $localize`:@@accessibilityDeactivateError:No s'ha pogut donar de baixa el registre.`;
export const ACCESSIBILITY_RESTORE_ERROR = $localize`:@@accessibilityRestoreError:No s'ha pogut restaurar el registre.`;
export const ACCESSIBILITY_FORBIDDEN = $localize`:@@accessibilityForbidden:No tens permisos per fer aquesta operació.`;

export const ACCESSIBILITY_DEACTIVATE_DIALOG_TITLE = $localize`:@@accessibilityDeactivateDialogTitle:Donar de baixa el registre?`;
export const ACCESSIBILITY_DEACTIVATE_DIALOG_MESSAGE = (name: string) =>
  $localize`:@@accessibilityDeactivateDialogMessage:Estàs a punt de donar de baixa «${name}:resourceName:».`;
export const ACCESSIBILITY_DEACTIVATE_DIALOG_CANCEL_LABEL = $localize`:@@accessibilityDeactivateDialogCancel:Cancel·lar`;
export const ACCESSIBILITY_DEACTIVATE_DIALOG_CONFIRM_LABEL = $localize`:@@accessibilityDeactivateDialogConfirm:Donar de baixa`;
export const ACCESSIBILITY_DEACTIVATE_DIALOG_CANCEL_ARIA_LABEL = $localize`:@@accessibilityDeactivateDialogCancelAria:Cancel·la la baixa del registre`;
export const ACCESSIBILITY_DEACTIVATE_DIALOG_CONFIRM_ARIA_LABEL = $localize`:@@accessibilityDeactivateDialogConfirmAria:Confirma la baixa del registre`;

export const ACCESSIBILITY_DIALOG_TITLES = (
  singular: string,
): Record<CrudEntityDialogMode, string> => ({
  create: $localize`:@@accessibilityDialogCreateTitle:Afegir ${singular}:resource:`,
  view: $localize`:@@accessibilityDialogViewTitle:Consultar ${singular}:resource:`,
  edit: $localize`:@@accessibilityDialogEditTitle:Editar ${singular}:resource:`,
});

export const ACCESSIBILITY_DIALOG_ACTIONS = (singular: string): CrudEntityDialogAriaLabels => ({
  accept: $localize`:@@accessibilityDialogAcceptAria:Accepta la consulta: ${singular}:resource:`,
  add: $localize`:@@accessibilityDialogAddAria:Afegeix: ${singular}:resource:`,
  cancel: $localize`:@@accessibilityDialogCancelAria:Cancel·la els canvis: ${singular}:resource:`,
  close: $localize`:@@accessibilityDialogCloseAria:Tanca el formulari: ${singular}:resource:`,
  deactivate: $localize`:@@accessibilityDialogDeactivateAria:Dona de baixa: ${singular}:resource:`,
  edit: $localize`:@@accessibilityDialogEditAria:Edita: ${singular}:resource:`,
  restore: $localize`:@@accessibilityDialogRestoreAria:Restaura: ${singular}:resource:`,
  save: $localize`:@@accessibilityDialogSaveAria:Desa els canvis: ${singular}:resource:`,
});

export const ACCESSIBILITY_DIALOG_LOADING = $localize`:@@accessibilityDialogLoading:Carregant el registre`;
export const ACCESSIBILITY_REQUIRED_ERROR = $localize`:@@accessibilityRequiredError:Camp obligatori`;
export const ACCESSIBILITY_NAME_MAX_LENGTH_ERROR = $localize`:@@accessibilityNameMaxLengthError:El nom ha de tenir com a màxim 150 caràcters`;
export const ACCESSIBILITY_NAME_ES_MAX_LENGTH_ERROR = $localize`:@@accessibilityNameEsMaxLengthError:El nom en castellà ha de tenir com a màxim 100 caràcters`;

export const ACCESSIBILITY_TABLE_ACTIONS_ARIA_LABEL = $localize`:@@accessibilityTableActionsAria:Obrir les accions del registre`;
export const ACCESSIBILITY_TABLE_VIEW_LABEL = $localize`:@@accessibilityTableView:Consulta`;
export const ACCESSIBILITY_TABLE_EDIT_LABEL = $localize`:@@accessibilityTableEdit:Edita`;
export const ACCESSIBILITY_TABLE_DEACTIVATE_LABEL = $localize`:@@accessibilityTableDeactivate:Dona de baixa`;
