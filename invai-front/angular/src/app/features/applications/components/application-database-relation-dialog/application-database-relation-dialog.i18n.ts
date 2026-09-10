import { CrudEntityDialogMode } from '@components/crud-entity-dialog/crud-entity-dialog';

export const APPLICATION_DATABASE_RELATION_DIALOG_TITLES: Record<
  Exclude<CrudEntityDialogMode, 'edit'>,
  string
> = {
  create: $localize`Afegir base de dades a l'aplicació`,
  view: $localize`Consultar base de dades de l'aplicació`,
};

export const APPLICATION_DATABASE_RELATION_DIALOG_REQUIRED = $localize`Selecciona una base de dades.`;

export const APPLICATION_DATABASE_RELATION_DIALOG_ARIA_LABELS = {
  accept: $localize`Accepta la consulta de la base de dades de l'aplicació`,
  add: $localize`Afegeix la base de dades a l'aplicació`,
  cancel: $localize`Cancel·la els canvis de la base de dades de l'aplicació`,
  close: $localize`Tanca el diàleg de la base de dades de l'aplicació`,
  deactivate: $localize`Dona de baixa la base de dades de l'aplicació`,
  edit: $localize`Edita la base de dades de l'aplicació`,
  restore: $localize`Restaura la base de dades de l'aplicació`,
  save: $localize`Desa els canvis de la base de dades de l'aplicació`,
};

export const APPLICATION_DATABASE_CATALOG_SEARCH_LABEL = $localize`Cerca ràpida de bases de dades`;
export const APPLICATION_DATABASE_CATALOG_FILTER_LABELS = {
  server: $localize`Servidor`,
  service: $localize`Servei / SID`,
  databaseType: $localize`Proveïdor`,
  status: $localize`Estat`,
};
