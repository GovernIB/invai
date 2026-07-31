import { InfrastructureDialogMode } from '../../systems.model';

export const DATABASE_MAINTENANCE_DIALOG_TITLES: Record<InfrastructureDialogMode, string> = {
  create: $localize`Afegir base de dades`,
  view: $localize`Consultar base de dades`,
  edit: $localize`Editar base de dades`,
};

export const DATABASE_MAINTENANCE_DIALOG_LABELS = {
  server: $localize`Servidor de BD`,
  service: $localize`Servei / SID`,
  port: $localize`Port`,
  databaseType: $localize`Proveïdor`,
  observations: $localize`Observacions`,
};

export const DATABASE_MAINTENANCE_DIALOG_RESTORE_ARIA_LABEL =
  $localize`Restaura la base de dades`;
export const DATABASE_MAINTENANCE_DIALOG_ACCEPT_ARIA_LABEL =
  $localize`Accepta la consulta de la base de dades`;
export const DATABASE_MAINTENANCE_DIALOG_ADD_ARIA_LABEL =
  $localize`Afegeix la base de dades`;
export const DATABASE_MAINTENANCE_DIALOG_CANCEL_ARIA_LABEL =
  $localize`Cancel·la els canvis de la base de dades`;
export const DATABASE_MAINTENANCE_DIALOG_CLOSE_ARIA_LABEL =
  $localize`Tanca el formulari de la base de dades`;
export const DATABASE_MAINTENANCE_DIALOG_DEACTIVATE_ARIA_LABEL =
  $localize`Dona de baixa la base de dades`;
export const DATABASE_MAINTENANCE_DIALOG_EDIT_ARIA_LABEL =
  $localize`Edita la base de dades`;
export const DATABASE_MAINTENANCE_DIALOG_SAVE_ARIA_LABEL =
  $localize`Desa els canvis de la base de dades`;
export const DATABASE_MAINTENANCE_DIALOG_REQUIRED = $localize`Aquest camp és obligatori.`;
export const DATABASE_MAINTENANCE_DIALOG_PORT =
  $localize`El port ha de ser un nombre enter entre 1 i 65535.`;
