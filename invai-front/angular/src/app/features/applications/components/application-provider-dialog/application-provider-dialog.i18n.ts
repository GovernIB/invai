import {
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';

export const APPLICATION_PROVIDER_DIALOG_TITLES: Record<
  CrudEntityDialogMode,
  string
> = {
  create: $localize`Afegir proveïdor`,
  view: $localize`Consultar proveïdor`,
  edit: $localize`Editar proveïdor`,
};

export const APPLICATION_PROVIDER_DIALOG_LABELS = {
  companyName: $localize`Raó social`,
  role: $localize`Rol`,
  startDate: $localize`Data inici`,
  expireDate: $localize`Data fi`,
};

export const APPLICATION_PROVIDER_DIALOG_REQUIRED_ERROR =
  $localize`Camp obligatori`;
export const APPLICATION_PROVIDER_DIALOG_MAX_LENGTH_ERROR =
  $localize`La raó social ha de tenir com a màxim 255 caràcters`;
export const APPLICATION_PROVIDER_DIALOG_DATE_RANGE_ERROR =
  $localize`La data de fi no pot ser anterior a la data d'inici`;
export const APPLICATION_PROVIDER_DIALOG_DATE_FORMAT = 'dd/mm/yy';
export const APPLICATION_PROVIDER_DIALOG_DATE_PLACEHOLDER = $localize`dd/mm/aaaa`;

export const APPLICATION_PROVIDER_DIALOG_ARIA_LABELS: CrudEntityDialogAriaLabels = {
  accept: $localize`Accepta la consulta del proveïdor`,
  add: $localize`Afegeix el proveïdor`,
  cancel: $localize`Cancel·la els canvis del proveïdor`,
  close: $localize`Tanca el formulari del proveïdor`,
  deactivate: $localize`Dona de baixa el proveïdor`,
  edit: $localize`Edita el proveïdor`,
  restore: $localize`Restaura el proveïdor`,
  save: $localize`Desa els canvis del proveïdor`,
};
