import { SelectOption } from '../../../../applications.model';
export const APPLICATION_ACCESSIBILITY_SECTION_TITLE = $localize`:@@applicationAccessibilitySectionTitle:Accessibilitat`;

export const APPLICATION_ACCESSIBILITY_LABELS = {
  classificationSegment: $localize`Segment de classificació`,
  complianceStatus: $localize`Situació de compliment`,
  reportExpirationDate: $localize`Caducitat del darrer informe`,
  mobileApplication: $localize`Aplicació mòbil`,
  mobileApplicationName: $localize`Nom aplicació mòbil`,
  publicUrl: $localize`URL pública`,
  inaccessibleContent: $localize`Contingut no accessible`,
  observations: $localize`Observacions`,
};

export const APPLICATION_ACCESSIBILITY_MOBILE_OPTIONS: SelectOption<boolean>[] = [
  { value: true, label: $localize`Sí` },
  { value: false, label: $localize`No` },
];

export const APPLICATION_ACCESSIBILITY_NO_VALUE = $localize`No informat`;
export const APPLICATION_ACCESSIBILITY_URL_ERROR = $localize`Introdueix una URL HTTP o HTTPS vàlida`;
export const APPLICATION_ACCESSIBILITY_DATE_FORMAT = 'dd/mm/yy';
export const APPLICATION_ACCESSIBILITY_DATE_PLACEHOLDER = $localize`dd/mm/aaaa`;
export const APPLICATION_ACCESSIBILITY_MOBILE_NAME_UNAVAILABLE = $localize`Disponible quan l'aplicació mòbil està marcada com a «Sí».`;
export const APPLICATION_ACCESSIBILITY_PUBLIC_URL_ARIA_LABEL = $localize`Obrir la URL pública de l'aplicació`;
export const APPLICATION_ACCESSIBILITY_DOCUMENTATION_LABEL = $localize`Documentació`;
export const APPLICATION_ACCESSIBILITY_DOCUMENTATION_ARIA_LABEL = $localize`Obrir la documentació d'accessibilitat`;
export const APPLICATION_ACCESSIBILITY_INFO_TITLE = $localize`Informació`;
export const APPLICATION_ACCESSIBILITY_DOCUMENTATION_PENDING = $localize`La documentació d'accessibilitat encara no està implementada.`;

export const APPLICATION_ACCESSIBILITY_CATALOG_FAILED = $localize`:@@applicationAccessibilityCatalogFailed:No s'han pogut carregar els catàlegs d'accessibilitat. Torna-ho a provar.`;
export const APPLICATION_ACCESSIBILITY_CATALOG_FORBIDDEN = $localize`:@@applicationAccessibilityCatalogForbidden:El teu perfil no té permisos per consultar els catàlegs d'accessibilitat.`;
export const APPLICATION_ACCESSIBILITY_CATALOG_EMPTY = $localize`:@@applicationAccessibilityCatalogEmpty:No hi ha registres actius disponibles.`;
export const APPLICATION_ACCESSIBILITY_CATALOG_RETRY = $localize`:@@applicationAccessibilityCatalogRetry:Torna a carregar els catàlegs`;
export const APPLICATION_ACCESSIBILITY_CATALOG_FILTER = $localize`:@@applicationAccessibilityCatalogFilter:Cerca al catàleg`;
export const APPLICATION_ACCESSIBILITY_CATALOG_UNAVAILABLE = (id: number) =>
  $localize`:@@applicationAccessibilityCatalogUnavailable:Registre no disponible (ID ${id}:id:)`;

export const APPLICATION_ACCESSIBILITY_MESSAGES = {
  failed: $localize`:@@applicationAccessibilityLoadFailed:No s'han pogut carregar les dades d'accessibilitat. Torna-ho a provar.`,
  unavailable: $localize`:@@applicationAccessibilityUnavailable:No es pot identificar el registre d'accessibilitat d'aquesta aplicació. Torna a carregar les dades.`,
  forbidden: $localize`:@@applicationAccessibilityForbidden:El teu perfil no té permisos per fer aquesta operació d'accessibilitat.`,
  deleted: $localize`:@@applicationAccessibilityDeleted:El registre d'accessibilitat està donat de baixa i no es pot modificar.`,
  retry: $localize`:@@applicationAccessibilityRetry:Torna a carregar l'accessibilitat`,
  clearBlocked: $localize`:@@applicationAccessibilityClearBlocked:No es pot deixar en blanc una classificació o situació de compliment ja desada. Pots seleccionar un altre valor.`,
  maxLength: $localize`:@@applicationAccessibilityMaxLength:El valor no pot superar els 255 caràcters.`,
};
