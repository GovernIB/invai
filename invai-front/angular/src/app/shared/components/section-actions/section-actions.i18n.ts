export const SECTION_ACTIONS_QUICK_SEARCH_ARIA_LABEL = $localize`Cerca ràpida`;
export const SECTION_ACTIONS_CLEAR_QUICK_SEARCH_ARIA_LABEL = $localize`Neteja la cerca ràpida`;
export const SECTION_ACTIONS_COLUMNS_FILTER_PLACEHOLDER = $localize`Cerca columna`;
export const SECTION_ACTIONS_FILTERS_ARIA_LABEL = $localize`Mostra o amaga els filtres`;
// The visible badge is hidden from assistive technology, so the button name carries the count.
export const SECTION_ACTIONS_FILTERS_APPLIED_ARIA_LABEL = (label: string, count: number) =>
  count === 1
    ? $localize`${label}:label: (1 filtre aplicat)`
    : $localize`${label}:label: (${count}:count: filtres aplicats)`;
export const SECTION_ACTIONS_COLUMNS_ARIA_LABEL = $localize`Mostra o oculta columnes`;
export const SECTION_ACTIONS_EXPORT_ARIA_LABEL = $localize`Exporta a Excel`;
export const SECTION_ACTIONS_ADD_ARIA_LABEL = $localize`Afegeix`;
