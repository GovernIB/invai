export const APPLICATION_CREATE_LABELS = {
  application: $localize`Aplicació`,
  category: $localize`Categoria`,
  informationSystem: $localize`Sistema d'informació`,
  scope: $localize`Àmbit`,
  commissionSectionTitle: $localize`Comissió informàtica`,
  commissionName: $localize`Nom`,
  commissionExpedientNumber: $localize`Número d'expedient`,
  commissionApprovalDate: $localize`Data de la comissió informàtica`,
  commissionType: $localize`Tipus de comissió`,
  prefix: $localize`Prefix`,
  code: $localize`Codi`,
  administrativeUnit: $localize`Unitat administrativa`,
  description: $localize`Descripció (opcional)`,
};

export const APPLICATION_CREATE_REQUIRED_ERROR = $localize`Camp obligatori`;
export const APPLICATION_CREATE_PREFIX_MAX_LENGTH_ERROR =
  $localize`El prefix ha de tenir com a màxim 3 caràcters`;
export const APPLICATION_CREATE_CODE_MIN_LENGTH_ERROR =
  $localize`El codi ha de tenir com a mínim 4 caràcters`;
export const APPLICATION_CREATE_CODE_MAX_LENGTH_ERROR =
  $localize`El codi ha de tenir com a màxim 10 caràcters`;
export const APPLICATION_CREATE_SELECT_PLACEHOLDER = $localize`Selecciona una opció`;
export const APPLICATION_CREATE_SUBMIT_LABEL = $localize`Afegir aplicació`;
export const APPLICATION_CREATE_SUBMIT_ARIA_LABEL = $localize`Afegeix l'aplicació`;
export const APPLICATION_CREATE_BACK_LABEL = $localize`Tornar`;
export const APPLICATION_CREATE_BACK_ARIA_LABEL = $localize`Torna al llistat d'aplicacions`;
export const APPLICATION_CREATE_SUCCESS_TITLE = $localize`Aplicació afegida`;
export const APPLICATION_CREATE_SUCCESS_MESSAGE = (assignedRoles: number, prefix: string) => {
  if (assignedRoles === 0) {
    return $localize`Aplicació afegida correctament. No hi ha cap rol de Soffid amb el prefix ${prefix}:prefix:.`;
  }
  if (assignedRoles === 1) {
    return $localize`Aplicació afegida correctament amb 1 rol de Soffid assignat.`;
  }
  return $localize`Aplicació afegida correctament amb ${assignedRoles}:assignedRoles: rols de Soffid assignats.`;
};
export const APPLICATION_CREATE_ROLES_ERROR_MESSAGE =
  $localize`L'aplicació s'ha afegit, però no s'han pogut assignar els seus rols de Soffid.`;
export const APPLICATION_CREATE_ERROR_TITLE = $localize`Error`;
export const APPLICATION_CREATE_ERROR_MESSAGE = $localize`No s'ha pogut afegir l'aplicació.`;
