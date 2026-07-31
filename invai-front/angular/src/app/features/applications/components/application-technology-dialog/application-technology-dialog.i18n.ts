import {
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';

export const APPLICATION_TECHNOLOGY_DIALOG_TITLES: Record<
  CrudEntityDialogMode,
  string
> = {
  create: $localize`Afegir tecnologia`,
  view: $localize`Consultar tecnologia`,
  edit: $localize`Editar tecnologia`,
};

export const APPLICATION_TECHNOLOGY_DIALOG_LABELS = {
  technology: $localize`Tecnologia`,
  layer: $localize`Capa`,
  version: $localize`Versió`,
  architecture: $localize`Arquitectura`,
};

export const APPLICATION_TECHNOLOGY_DIALOG_REQUIRED_ERROR =
  $localize`Camp obligatori`;
export const APPLICATION_TECHNOLOGY_DIALOG_MAX_LENGTH_ERROR =
  $localize`El valor ha de tenir com a màxim 255 caràcters`;

export const APPLICATION_TECHNOLOGY_DIALOG_ARIA_LABELS: CrudEntityDialogAriaLabels = {
  accept: $localize`Accepta la consulta de la tecnologia`,
  add: $localize`Afegeix la tecnologia`,
  cancel: $localize`Cancel·la els canvis de la tecnologia`,
  close: $localize`Tanca el formulari de la tecnologia`,
  deactivate: $localize`Dona de baixa la tecnologia`,
  edit: $localize`Edita la tecnologia`,
  restore: $localize`Restaura la tecnologia`,
  save: $localize`Desa els canvis de la tecnologia`,
};
