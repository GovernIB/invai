import { KeyLabel } from '@models/table.model';

export const APPLICATION_DATA_SECTION_TITLE = $localize`:@@applicationDataTitle:Dades`;
export const APPLICATION_DATA_EMPTY_VALUE = '-';

export const APPLICATION_DATA_LABELS = {
  sources: $localize`:@@applicationDataSources:Fonts de dades`,
  openData: $localize`:@@applicationDataOpenData:Open Data`,
  reuse: $localize`:@@applicationDataReuse:Reutilització`,
  useCustomUrl: $localize`:@@applicationDataUseCustomUrl:Utilitza una URL pròpia`,
  url: $localize`:@@applicationDataUrl:URL de consulta`,
  endpoints: $localize`:@@applicationDataEndpoints:Endpoints GET publicats`,
  observations: $localize`:@@applicationDataObservations:Observacions`,
  yes: $localize`:@@applicationDataYes:Sí`,
  no: $localize`:@@applicationDataNo:No`,
  noValue: $localize`:@@applicationDataNoValue:No informat`,
};

export const APPLICATION_DATA_URL_MESSAGES = {
  detectedHelp: $localize`:@@applicationDataDetectedUrlHelp:URL detectada automàticament a partir del codi de l'aplicació. Activa «Utilitza una URL pròpia» per indicar-ne una altra.`,
  required: $localize`:@@applicationDataUrlRequired:Indica la URL de consulta quan utilitzes una URL pròpia.`,
  maxLength: $localize`:@@applicationDataUrlMaxLength:La URL no pot superar els 500 caràcters.`,
  pattern: $localize`:@@applicationDataUrlPattern:Introdueix una URL HTTP o HTTPS vàlida.`,
};

export const APPLICATION_DATA_MESSAGES = {
  failed: $localize`:@@applicationDataLoadFailed:No s'han pogut carregar les dades de l'aplicació. Torna-ho a provar.`,
  unavailable: $localize`:@@applicationDataUnavailable:No es pot identificar el registre de dades d'aquesta aplicació. Torna a carregar les dades.`,
  forbidden: $localize`:@@applicationDataForbidden:El teu perfil no té permisos per fer aquesta operació de dades.`,
  deleted: $localize`:@@applicationDataDeleted:El registre de dades està donat de baixa i no es pot modificar.`,
  absentEndpoints: $localize`:@@applicationDataAbsentEndpoints:Els endpoints publicats es consultaran quan es desin les dades de la pestanya.`,
  sourceUnavailable: $localize`:@@applicationDataSourceUnavailable:No s'ha pogut consultar el catàleg d'aquesta font en aquest moment. Torna-ho a provar.`,
  retry: $localize`:@@applicationDataRetry:Torna a carregar les dades`,
  infoTitle: $localize`:@@applicationDataInfoTitle:Informació`,
};

export const APPLICATION_DATA_TABLE_TEXTS = {
  toggleHeader: $localize`:@@applicationDataToggleHeader:Paràmetres de l'endpoint`,
  parametersOf: (path: string) =>
    $localize`:@@applicationDataParametersOf:Paràmetres de ${path}:path:`,
  noParameters: $localize`:@@applicationDataNoParameters:Sense paràmetres`,
  empty: $localize`:@@applicationDataEndpointsEmpty:L'aplicació no publica cap endpoint GET.`,
};

export const APPLICATION_DATA_ENDPOINT_COLUMNS: KeyLabel[] = [
  { key: 'path', label: $localize`:@@applicationDataColumnPath:Ruta`, minWidth: '14rem' },
  {
    key: 'summary',
    label: $localize`:@@applicationDataColumnSummary:Resum`,
    minWidth: '12rem',
    wrap: true,
    maxWidth: '24rem',
  },
  {
    key: 'description',
    label: $localize`:@@applicationDataColumnDescription:Descripció`,
    minWidth: '14rem',
    wrap: true,
    maxWidth: '32rem',
  },
  {
    key: 'parameters',
    label: $localize`:@@applicationDataColumnParameters:Paràmetres`,
    minWidth: '7rem',
  },
];

export const APPLICATION_DATA_PARAMETER_COLUMNS: KeyLabel[] = [
  { key: 'name', label: $localize`:@@applicationDataParameterName:Nom`, minWidth: '10rem' },
  { key: 'in', label: $localize`:@@applicationDataParameterIn:Ubicació`, minWidth: '6rem' },
  {
    key: 'required',
    label: $localize`:@@applicationDataParameterRequired:Obligatori`,
    minWidth: '6rem',
  },
  { key: 'type', label: $localize`:@@applicationDataParameterType:Tipus`, minWidth: '6rem' },
  { key: 'format', label: $localize`:@@applicationDataParameterFormat:Format`, minWidth: '6rem' },
  {
    key: 'defaultValue',
    label: $localize`:@@applicationDataParameterDefault:Valor per defecte`,
    minWidth: '8rem',
  },
  {
    key: 'enumValues',
    label: $localize`:@@applicationDataParameterEnum:Valors permesos`,
    minWidth: '10rem',
    wrap: true,
    maxWidth: '24rem',
  },
  {
    key: 'description',
    label: $localize`:@@applicationDataParameterDescription:Descripció`,
    minWidth: '14rem',
    wrap: true,
    maxWidth: '32rem',
  },
];
