export const APPLICATIONS_TITLE = $localize`Aplicacions`;
export const APPLICATIONS_FILTER_PREFIX = $localize`Prefix`;
export const APPLICATIONS_FILTER_APPLICATION = $localize`Aplicació`;
export const APPLICATIONS_FILTER_CATEGORY = $localize`Categoria`;
export const APPLICATIONS_FILTER_INFORMATION_SYSTEM = $localize`Sistema d'informació`;
export const APPLICATIONS_FILTER_SCOPE = $localize`Àmbit`;
export const APPLICATIONS_FILTER_ADMINISTRATIVE_UNIT = $localize`Ut. Administrativa`;
export const APPLICATIONS_FILTER_STATUS = $localize`Estat`;
export const APPLICATIONS_FILTER_RESPONSIBLE = $localize`:@@applicationsFilterResponsible:Responsable`;
export const APPLICATIONS_FILTER_RESPONSIBLE_EMPTY = $localize`No s'han trobat persones actives`;
export const APPLICATIONS_FILTER_RESPONSIBLE_LOADING = $localize`Cercant persones responsables…`;
export const APPLICATIONS_FILTER_DATABASE = $localize`Bases de dades`;
export const APPLICATIONS_FILTER_SERVER = $localize`Servidor`;
export const APPLICATIONS_FILTER_ENVIRONMENT = $localize`Entorn`;
export const APPLICATIONS_FILTER_INCOMPLETE = $localize`Incomplets`;
export const APPLICATIONS_LOAD_ERROR_SUMMARY = $localize`Error`;
export const APPLICATIONS_LOAD_ERROR_DETAIL = $localize`No s'han pogut carregar les aplicacions.`;
export const APPLICATIONS_RESPONSIBLE_SEARCH_ERROR_DETAIL = $localize`No s'han pogut cercar les persones responsables.`;
export const APPLICATIONS_QUICK_SEARCH_ARIA_LABEL = $localize`Cerca ràpida d'aplicacions`;
export const APPLICATIONS_EXPORT_ARIA_LABEL = $localize`Exporta aplicacions a Excel`;
export const APPLICATIONS_ADD_ARIA_LABEL = $localize`Afegeix una aplicació`;
export const APPLICATIONS_DIR3_CHECK_LABEL = $localize`:@@applicationsDir3CheckLabel:Validar DIR3 de totes`;
export const APPLICATIONS_DIR3_CHECK_PROGRESS = $localize`:@@applicationsDir3CheckProgress:Validant el DIR3 de totes les aplicacions…`;
export const APPLICATIONS_DIR3_CHECK_SUCCESS = $localize`:@@applicationsDir3CheckSuccess:Validació DIR3 completada`;
export const APPLICATIONS_DIR3_CHECK_MISMATCHES = (count: number) =>
  $localize`:@@applicationsDir3CheckMismatches:Aplicacions amb discrepàncies DIR3: ${count}.`;
export const APPLICATIONS_DIR3_CHECK_ERROR = $localize`:@@applicationsDir3CheckError:No s'ha pogut completar la validació DIR3.`;
export const APPLICATIONS_DIR3_AUDIT_STARTING_LABEL = $localize`:@@applicationsDir3AuditStartingLabel:Validant DIR3…`;
export const APPLICATIONS_DIR3_AUDIT_PROGRESS_LABEL = (processed: number, total: number) =>
  $localize`:@@applicationsDir3AuditProgressLabel:Validant DIR3: ${processed} de ${total}`;
export const APPLICATIONS_DIR3_AUDIT_RESULT_WITH_FAILURES = (mismatches: number, failed: number) =>
  $localize`:@@applicationsDir3AuditResultWithFailures:Aplicacions amb discrepàncies DIR3: ${mismatches}. Aplicacions que no s'han pogut validar: ${failed}.`;
export const APPLICATIONS_DIR3_AUDIT_STATUS_ERROR = $localize`:@@applicationsDir3AuditStatusError:No s'ha pogut consultar el progrés de la validació DIR3.`;

// The back-end reports why a background DIR3 audit failed as a message key, not a localized text.
const APPLICATIONS_DIR3_AUDIT_FAILURE_REASONS: Readonly<Record<string, string>> = {
  'exception.dir3audit.session': $localize`:@@applicationsDir3AuditSessionFailure:La validació DIR3 s'ha aturat perquè la teva sessió ha caducat o no es pot renovar. Tanca la sessió, torna a entrar i torna-ho a provar.`,
  'exception.dir3audit.soffid': $localize`:@@applicationsDir3AuditSoffidFailure:La validació DIR3 no ha pogut començar perquè no s'han pogut consultar les dades de Soffid. Torna-ho a provar més tard.`,
};

export function dir3AuditFailureMessage(reason: string | null): string {
  return reason && Object.hasOwn(APPLICATIONS_DIR3_AUDIT_FAILURE_REASONS, reason)
    ? APPLICATIONS_DIR3_AUDIT_FAILURE_REASONS[reason]
    : APPLICATIONS_DIR3_CHECK_ERROR;
}
