import { InfrastructureDialogMode } from '../../systems.model';

export const DATABASE_VENDOR_DIALOG_TITLES: Record<
  InfrastructureDialogMode,
  string
> = {
  create: $localize`Afegir proveïdor de base de dades`,
  view: $localize`Consultar proveïdor de base de dades`,
  edit: $localize`Editar proveïdor de base de dades`,
};

export const DATABASE_VENDOR_DIALOG_LABELS = {
  name: $localize`Proveïdor`,
  defaultPort: $localize`Port per defecte`,
};
export const DATABASE_VENDOR_DIALOG_REQUIRED =
  $localize`Aquest camp és obligatori.`;
export const DATABASE_VENDOR_DIALOG_PORT =
  $localize`El port ha de ser un nombre enter entre 1 i 65535.`;
export const DATABASE_VENDOR_DIALOG_ARIA_LABELS = {
  accept: $localize`Accepta la consulta del proveïdor`,
  add: $localize`Afegeix el proveïdor`,
  cancel: $localize`Cancel·la els canvis del proveïdor`,
  close: $localize`Tanca el formulari del proveïdor`,
  deactivate: $localize`Dona de baixa el proveïdor`,
  edit: $localize`Edita el proveïdor`,
  restore: $localize`Restaura el proveïdor`,
  save: $localize`Desa els canvis del proveïdor`,
};
