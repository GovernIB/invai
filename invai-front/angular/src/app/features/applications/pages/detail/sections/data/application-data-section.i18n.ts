export const APPLICATION_DATA_SECTION_TITLE = $localize`:@@applicationDataTitle:Dades`;

export const APPLICATION_DATA_LABELS = {
  openData: $localize`:@@applicationDataOpenData:Dades obertes`,
  reuse: $localize`:@@applicationDataReuse:Reutilització`,
  currentUrl: $localize`:@@applicationDataCurrentUrl:URL`,
  noUrl: $localize`:@@applicationDataNoUrl:No disponible`,
  opensInNewTab: $localize`:@@applicationDataOpensInNewTab:(s'obre en una pestanya nova)`,
  manualUrl: $localize`:@@applicationDataManualUrl:Indicar la URL manualment`,
  newUrl: $localize`:@@applicationDataNewUrl:Nova URL`,
  observations: $localize`:@@applicationDataObservations:Observacions`,
  noValue: $localize`:@@applicationDataNoValue:No informat`,
};

export const APPLICATION_DATA_URL_MESSAGES = {
  checking: $localize`:@@applicationDataUrlChecking:Comprovant si aquesta URL respon…`,
  responding: $localize`:@@applicationDataUrlResponding:Aquesta URL respon.`,
  notResponding: $localize`:@@applicationDataUrlNotResponding:Aquesta URL no respon.`,
  notChecked: $localize`:@@applicationDataUrlNotChecked:No s'ha pogut comprovar la URL d'aquesta font.`,
  manualHint: $localize`:@@applicationDataUrlManualHint:Edita les dades per indicar-ne una manualment.`,
  manualHintEditing: $localize`:@@applicationDataUrlManualHintEditing:Marca «Indicar la URL manualment» per indicar-ne una altra.`,
  pending: $localize`:@@applicationDataUrlPending:Es coneixerà quan es desin les dades de la pestanya.`,
  help: $localize`:@@applicationDataUrlHelp:Ha de començar per http:// o https://. En desar es comprovarà si respon.`,
  required: $localize`:@@applicationDataUrlRequiredManual:Indica la nova URL o desmarca «Indicar la URL manualment».`,
  maxLength: $localize`:@@applicationDataUrlMaxLength:La URL no pot superar els 500 caràcters.`,
  pattern: $localize`:@@applicationDataUrlPattern:Introdueix una URL HTTP o HTTPS vàlida.`,
};

export const APPLICATION_DATA_MESSAGES = {
  failed: $localize`:@@applicationDataLoadFailed:No s'han pogut carregar les dades de l'aplicació. Torna-ho a provar.`,
  unavailable: $localize`:@@applicationDataUnavailable:No es pot identificar el registre de dades d'aquesta aplicació. Torna a carregar les dades.`,
  forbidden: $localize`:@@applicationDataForbidden:El teu perfil no té permisos per fer aquesta operació de dades.`,
  deleted: $localize`:@@applicationDataDeleted:El registre de dades està donat de baixa i no es pot modificar.`,
  retry: $localize`:@@applicationDataRetry:Torna a carregar les dades`,
  infoTitle: $localize`:@@applicationDataInfoTitle:Informació`,
};

// A URL typed in by hand is saved even if it does not answer; the user is told which ones.
export const APPLICATION_DATA_SAVED_INVALID_URLS_MESSAGE = (sources: string[]) =>
  sources.length === 1
    ? $localize`:@@applicationDataSavedInvalidUrl:S'han desat les dades, però la URL de ${sources[0]}:source: no respon. Comprova-la o indica'n una altra.`
    : $localize`:@@applicationDataSavedInvalidUrls:S'han desat les dades, però les URL de ${sources[0]}:first: i de ${sources[1]}:second: no responen. Comprova-les o indica'n unes altres.`;
