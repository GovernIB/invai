import { ServerTypeCode } from '../../systems.model';

export const PHYSICAL_SERVERS_COPY: Record<
  ServerTypeCode,
  {
    ariaLabel: string;
    addAriaLabel: string;
    quickSearchPlaceholder: string;
    deleteTitle: string;
    deleteMessage: (id: number) => string;
    createTitle: string;
    createDetail: string;
    updateTitle: string;
    updateDetail: string;
    deleteSuccessTitle: string;
    deleteSuccessDetail: string;
    restoreTitle: string;
    restoreDetail: string;
  }
> = {
  APPLICATION: {
    ariaLabel: $localize`Servidors físics`,
    addAriaLabel: $localize`Afegir servidor físic`,
    quickSearchPlaceholder: $localize`Cerca ràpida de servidors físics`,
    deleteTitle: $localize`Donar de baixa el servidor físic?`,
    deleteMessage: (id) => $localize`El servidor físic #${id} quedarà inactiu.`,
    createTitle: $localize`Servidor físic afegit`,
    createDetail: $localize`El servidor físic s'ha afegit correctament.`,
    updateTitle: $localize`Servidor físic actualitzat`,
    updateDetail: $localize`El servidor físic s'ha actualitzat correctament.`,
    deleteSuccessTitle: $localize`Servidor físic donat de baixa`,
    deleteSuccessDetail:
      $localize`El servidor físic s'ha donat de baixa correctament.`,
    restoreTitle: $localize`Servidor físic restaurat`,
    restoreDetail: $localize`El servidor físic s'ha restaurat correctament.`,
  },
  DATABASE: {
    ariaLabel: $localize`Servidors de bases de dades`,
    addAriaLabel: $localize`Afegir servidor de base de dades`,
    quickSearchPlaceholder:
      $localize`Cerca ràpida de servidors de bases de dades`,
    deleteTitle: $localize`Donar de baixa el servidor de base de dades?`,
    deleteMessage: (id) =>
      $localize`El servidor de base de dades #${id} quedarà inactiu.`,
    createTitle: $localize`Servidor de base de dades afegit`,
    createDetail:
      $localize`El servidor de base de dades s'ha afegit correctament.`,
    updateTitle: $localize`Servidor de base de dades actualitzat`,
    updateDetail:
      $localize`El servidor de base de dades s'ha actualitzat correctament.`,
    deleteSuccessTitle: $localize`Servidor de base de dades donat de baixa`,
    deleteSuccessDetail:
      $localize`El servidor de base de dades s'ha donat de baixa correctament.`,
    restoreTitle: $localize`Servidor de base de dades restaurat`,
    restoreDetail:
      $localize`El servidor de base de dades s'ha restaurat correctament.`,
  },
};

export const PHYSICAL_SERVERS_LOAD_ERROR_SUMMARY = $localize`Error`;
export const PHYSICAL_SERVERS_LOAD_ERROR_DETAIL =
  $localize`No s'han pogut carregar els servidors.`;
export const PHYSICAL_SERVERS_CATALOG_LOAD_ERROR_DETAIL =
  $localize`No s'han pogut carregar els entorns actius.`;
export const PHYSICAL_SERVERS_SERVER_TYPE_LOAD_ERROR_DETAIL =
  $localize`No s'ha pogut carregar el tipus de servidor.`;
export const PHYSICAL_SERVERS_SAVE_ERROR_DETAIL =
  $localize`No s'ha pogut desar el servidor.`;
export const PHYSICAL_SERVERS_DELETE_ERROR_DETAIL =
  $localize`No s'ha pogut donar de baixa el servidor.`;
export const PHYSICAL_SERVERS_RESTORE_ERROR_DETAIL =
  $localize`No s'ha pogut restaurar el servidor.`;
export const PHYSICAL_SERVERS_DELETE_CANCEL = $localize`Cancel·la`;
export const PHYSICAL_SERVERS_DELETE_CONFIRM = $localize`Dona de baixa`;
export const PHYSICAL_SERVERS_QUICK_SEARCH_ARIA_LABEL =
  $localize`Cerca ràpida de servidors`;
