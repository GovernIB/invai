import {
  InfrastructureDialogMode,
  ServerTypeCode,
} from '../../systems.model';

export const PHYSICAL_SERVER_DIALOG_TITLES: Record<
  ServerTypeCode,
  Record<InfrastructureDialogMode, string>
> = {
  APPLICATION: {
    create: $localize`Afegir servidor físic`,
    view: $localize`Consultar servidor físic`,
    edit: $localize`Editar servidor físic`,
  },
  DATABASE: {
    create: $localize`Afegir servidor de base de dades`,
    view: $localize`Consultar servidor de base de dades`,
    edit: $localize`Editar servidor de base de dades`,
  },
};

export const PHYSICAL_SERVER_DIALOG_LABELS = {
  name: $localize`Servidor`,
  environment: $localize`Entorn`,
};
export const PHYSICAL_SERVER_DIALOG_REQUIRED =
  $localize`Aquest camp és obligatori.`;

export const PHYSICAL_SERVER_DIALOG_ARIA_LABELS = {
  accept: $localize`Accepta la consulta del servidor`,
  add: $localize`Afegeix el servidor`,
  cancel: $localize`Cancel·la els canvis del servidor`,
  close: $localize`Tanca el formulari del servidor`,
  deactivate: $localize`Dona de baixa el servidor`,
  edit: $localize`Edita el servidor`,
  restore: $localize`Restaura el servidor`,
  save: $localize`Desa els canvis del servidor`,
};
