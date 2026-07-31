import { InfrastructureDialogMode } from '../../systems.model';

export const SERVER_MAINTENANCE_DIALOG_TITLES: Record<InfrastructureDialogMode, string> = {
  create: $localize`Afegir servidor d'aplicacions`,
  view: $localize`Consultar servidor d'aplicacions`,
  edit: $localize`Editar servidor d'aplicacions`,
};

export const SERVER_MAINTENANCE_DIALOG_LABELS = {
  environment: $localize`Entorn`,
  server: $localize`Servidor`,
  instance: $localize`Instància`,
  port: $localize`Port`,
  version: $localize`Versió`,
  observations: $localize`Observacions`,
};

export const SERVER_MAINTENANCE_DIALOG_RESTORE_ARIA_LABEL =
  $localize`Restaura el servidor d'aplicacions`;
export const SERVER_MAINTENANCE_DIALOG_ACCEPT_ARIA_LABEL =
  $localize`Accepta la consulta del servidor d'aplicacions`;
export const SERVER_MAINTENANCE_DIALOG_ADD_ARIA_LABEL =
  $localize`Afegeix el servidor d'aplicacions`;
export const SERVER_MAINTENANCE_DIALOG_CANCEL_ARIA_LABEL =
  $localize`Cancel·la els canvis del servidor d'aplicacions`;
export const SERVER_MAINTENANCE_DIALOG_CLOSE_ARIA_LABEL =
  $localize`Tanca el formulari del servidor d'aplicacions`;
export const SERVER_MAINTENANCE_DIALOG_DEACTIVATE_ARIA_LABEL =
  $localize`Dona de baixa el servidor d'aplicacions`;
export const SERVER_MAINTENANCE_DIALOG_EDIT_ARIA_LABEL =
  $localize`Edita el servidor d'aplicacions`;
export const SERVER_MAINTENANCE_DIALOG_SAVE_ARIA_LABEL =
  $localize`Desa els canvis del servidor d'aplicacions`;
export const SERVER_MAINTENANCE_DIALOG_REQUIRED = $localize`Aquest camp és obligatori.`;
export const SERVER_MAINTENANCE_DIALOG_PORT =
  $localize`El port ha de ser un nombre enter entre 1 i 65535.`;
