import { CrudEntityDialogMode } from '@components/crud-entity-dialog/crud-entity-dialog';

export const APPLICATION_SYSTEM_RELATION_DIALOG_TITLES: Record<
  CrudEntityDialogMode,
  string
> = {
  create: $localize`Afegir servidor a l'aplicació`,
  view: $localize`Consultar servidor de l'aplicació`,
  edit: $localize`Editar servidor de l'aplicació`,
};

export const APPLICATION_SYSTEM_RELATION_DIALOG_REQUIRED =
  $localize`Selecciona un servidor.`;

export const APPLICATION_SYSTEM_RELATION_DIALOG_ARIA_LABELS = {
  accept: $localize`Accepta la consulta del servidor de l'aplicació`,
  add: $localize`Afegeix el servidor a l'aplicació`,
  cancel: $localize`Cancel·la els canvis del servidor de l'aplicació`,
  close: $localize`Tanca el diàleg del servidor de l'aplicació`,
  deactivate: $localize`Dona de baixa el servidor de l'aplicació`,
  edit: $localize`Edita el servidor de l'aplicació`,
  restore: $localize`Restaura el servidor de l'aplicació`,
  save: $localize`Desa els canvis del servidor de l'aplicació`,
};
