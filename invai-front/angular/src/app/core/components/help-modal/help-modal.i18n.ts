export const HELP_MODAL_LABELS = {
  title: $localize`Centre d'ajuda`,
  close: $localize`Tanca el centre d'ajuda`,
  searchAriaLabel: $localize`Cerca en tota l'ajuda`,
  topicsAriaLabel: $localize`Temes d'ajuda`,
  searchPlaceholder: $localize`Cercar en tota l'ajuda...`,
  emptyMessage: $localize`No s'han trobat resultats`,
  emptySelection: $localize`Seleccioneu un tema d'ajuda a la llista de l'esquerra`,
  loadingTopics: $localize`Carregant els temes d'ajuda`,
  topicsError: $localize`No s'han pogut carregar els temes d'ajuda.`,
  noTopics: $localize`Encara no hi ha temes d'ajuda.`,
  loadingDocument: $localize`Carregant el tema d'ajuda`,
  documentError: $localize`No s'ha pogut carregar el tema d'ajuda.`,
  retry: $localize`Reintentar`,
  openTopic: $localize`Tema obert`,
  // Same text, and so the same translation, as the button of the user menu.
  openHelp: $localize`Obrir ajuda`,
  // Same wording as the other links of the app that open in a new tab.
  opensInNewTab: $localize`(s'obre en una pestanya nova)`,
};

/** Status message with the number of topics a search finds. */
export const HELP_MODAL_RESULTS_FOUND = (count: number) =>
  count === 1 ? $localize`1 tema trobat` : $localize`${count}:count: temes trobats`;
