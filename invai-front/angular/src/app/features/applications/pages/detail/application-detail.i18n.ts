export const APPLICATION_DETAIL_ACTIVATION_LABEL = $localize`Donar d'alta`;
export const APPLICATION_DETAIL_RESTORE_LABEL = $localize`Restaurar`;
export const APPLICATION_DETAIL_ACTIVATION_ARIA_LABEL = $localize`Donar d'alta l'aplicació`;
export const APPLICATION_DETAIL_INACTIVE_NOTE = $localize`L'aplicació està donada de baixa`;
export const APPLICATION_DETAIL_ACTIVATION_DIALOG_TITLE = $localize`Donar d'alta l'aplicació?`;
export const APPLICATION_DETAIL_ACTIVATION_DIALOG_CANCEL_LABEL = $localize`Cancel·la`;
export const APPLICATION_DETAIL_ACTIVATION_DIALOG_CONFIRM_LABEL =
  APPLICATION_DETAIL_ACTIVATION_LABEL;
export const APPLICATION_DETAIL_ACTIVATION_DIALOG_CANCEL_ARIA_LABEL = $localize`Cancel·la l'alta de l'aplicació`;
export const APPLICATION_DETAIL_ACTIVATION_DIALOG_CONFIRM_ARIA_LABEL = $localize`Confirma l'alta de l'aplicació`;
export const APPLICATION_DETAIL_ACTIVATION_DIALOG_MESSAGE = (applicationName: string) =>
  $localize`Estàs a punt de donar d'alta «${applicationName}:applicationName:».`;
export const APPLICATION_DETAIL_ACTIVATION_SUCCESS_TITLE = $localize`Aplicació donada d'alta`;
export const APPLICATION_DETAIL_ACTIVATION_SUCCESS_MESSAGE = $localize`L'aplicació s'ha donat d'alta correctament.`;
export const APPLICATION_DETAIL_ACTIVATION_ERROR_MESSAGE = $localize`No s'ha pogut donar d'alta l'aplicació.`;
export const APPLICATION_DETAIL_SECTIONS_ARIA_LABEL = $localize`Seccions de l'aplicació`;
export const APPLICATION_DETAIL_COMPLETENESS_REFRESH_ERROR = $localize`:@@applicationCompletenessRefreshError:No s'han pogut actualitzar els indicadors de dades incompletes. Es mantenen els últims valors rebuts del servidor.`;
export const APPLICATION_DETAIL_COMPLETENESS_RETRY = $localize`:@@applicationCompletenessRetry:Torna a carregar els indicadors`;
export const APPLICATION_DETAIL_RESPONSIBLE_TYPES_INCOMPLETE = $localize`La secció Responsables està incompleta: falten tipus de responsable per assignar.`;
export const APPLICATION_DETAIL_AUTHORIZED_INCOMPLETE = $localize`La secció Responsables està incompleta: no hi ha cap persona autoritzada.`;
export const APPLICATION_DETAIL_RESPONSIBLE_AND_AUTHORIZED_INCOMPLETE = $localize`La secció Responsables està incompleta: falten tipus de responsable per assignar i no hi ha cap persona autoritzada.`;
export const APPLICATION_DETAIL_DEVELOPMENT_INCOMPLETE = $localize`La secció Desenvolupament està incompleta: falten camps obligatoris.`;
export const APPLICATION_DETAIL_SYSTEMS_INCOMPLETE = $localize`La secció Sistemes i BD està incompleta: no hi ha cap sistema actiu.`;
export const APPLICATION_DETAIL_DATABASES_INCOMPLETE = $localize`La secció Sistemes i BD està incompleta: no hi ha cap base de dades activa.`;
export const APPLICATION_DETAIL_SYSTEMS_AND_DATABASES_INCOMPLETE = $localize`La secció Sistemes i BD està incompleta: no hi ha cap sistema ni cap base de dades actius.`;
export const APPLICATION_DETAIL_ACCESSIBILITY_INCOMPLETE = $localize`La secció Accessibilitat està incompleta: falten camps obligatoris.`;
export const APPLICATION_DETAIL_SECURITY_INCOMPLETE = $localize`La secció Seguretat està incompleta: falta almenys un context web, una classificació ENS o un risc actiu.`;
export const APPLICATION_DETAIL_SECURITY_INCONSISTENT = $localize`La secció Seguretat conté més d'una classificació ENS. Cal corregir les dades al servidor.`;
export const APPLICATION_DETAIL_SECURITY_UNKNOWN = $localize`No s'ha pogut comprovar si la secció Seguretat està completa.`;
export const APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE = $localize`Canvis desats`;
export const APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_MESSAGE = (sectionLabel: string) =>
  $localize`S'han desat correctament els canvis de ${sectionLabel}:sectionLabel:.`;
export const APPLICATION_DETAIL_SECTION_SAVE_MOCKED_MESSAGE = (sectionLabel: string) =>
  $localize`Els canvis de ${sectionLabel}:sectionLabel: s'han conservat localment, però encara no s'han enviat al servidor.`;
export const APPLICATION_DETAIL_SAVE_ERROR_TITLE = $localize`Error`;
export const APPLICATION_DETAIL_SAVE_ERROR_MESSAGE = $localize`No s'han pogut desar els canvis.`;
export const APPLICATION_DETAIL_ACTIVATION_ERROR_TITLE = APPLICATION_DETAIL_SAVE_ERROR_TITLE;
export const APPLICATION_DETAIL_WITHDRAWAL_LABEL = $localize`Donar de baixa`;
export const APPLICATION_DETAIL_WITHDRAWAL_ARIA_LABEL = $localize`Donar de baixa l'aplicació`;
export const APPLICATION_DETAIL_WITHDRAWAL_DIALOG_TITLE = $localize`Donar de baixa l'aplicació?`;
export const APPLICATION_DETAIL_WITHDRAWAL_DIALOG_CANCEL_LABEL = $localize`Cancel·la`;
export const APPLICATION_DETAIL_WITHDRAWAL_DIALOG_CONFIRM_LABEL = $localize`Donar de baixa`;
export const APPLICATION_DETAIL_WITHDRAWAL_DIALOG_CANCEL_ARIA_LABEL = $localize`Cancel·la la baixa de l'aplicació`;
export const APPLICATION_DETAIL_WITHDRAWAL_DIALOG_CONFIRM_ARIA_LABEL = $localize`Confirma la baixa de l'aplicació`;
export const APPLICATION_DETAIL_WITHDRAWAL_DIALOG_MESSAGE = (applicationName: string) =>
  $localize`Estàs a punt de donar de baixa «${applicationName}:applicationName:». L'aplicació deixarà d'aparèixer al llistat.`;
export const APPLICATION_DETAIL_WITHDRAWAL_SUCCESS_TITLE = $localize`Aplicació donada de baixa`;
export const APPLICATION_DETAIL_WITHDRAWAL_SUCCESS_MESSAGE = $localize`L'aplicació s'ha donat de baixa correctament.`;
export const APPLICATION_DETAIL_WITHDRAWAL_ERROR_TITLE = APPLICATION_DETAIL_SAVE_ERROR_TITLE;
export const APPLICATION_DETAIL_WITHDRAWAL_ERROR_MESSAGE = $localize`No s'ha pogut donar de baixa l'aplicació.`;
export const APPLICATION_DETAIL_INFO_TITLE = $localize`Informació`;
export const APPLICATION_DETAIL_UNSAVED_CHANGES_TITLE = $localize`Canvis sense desar`;
export const APPLICATION_DETAIL_UNSAVED_CHANGES_MESSAGE = (sectionLabels: string) =>
  $localize`Has de desar o cancel·lar els canvis de les pestanyes següents abans de sortir:\n${sectionLabels}:sectionLabels:`;
export const APPLICATION_DETAIL_UNSAVED_CHANGES_CLOSE_LABEL = $localize`Entesos`;
export const APPLICATION_DETAIL_UNSAVED_CHANGES_CLOSE_ARIA_LABEL = $localize`Tanca l'avís de canvis sense desar`;

export const APPLICATION_DETAIL_TABS = {
  general: $localize`General`,
  responsible: $localize`Responsables`,
  systemsDatabases: $localize`Sistemes i BD`,
  development: $localize`Desenvolupament`,
  accessibility: $localize`Accessibilitat`,
  security: $localize`Seguretat`,
};
