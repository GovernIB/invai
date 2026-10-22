import { KeyLabel } from '@models/table.model';
import { MAINTENANCES_ROUTES_LOC } from '@features/maintenances/maintenances.routes.i18n';

export const DOCUMENT_CATEGORY_NAME_MAX_LENGTH = 150;

export const DOCUMENT_CATEGORIES_TABLE_COLUMNS: KeyLabel[] = [
  {
    key: 'name',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`:@@documentCategoriesColumnName:Nom`,
    sortBy: 'name',
    minWidth: '14rem',
  },
  {
    key: 'nameEs',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`:@@documentCategoriesColumnNameEs:Nom en castellà`,
    sortBy: 'nameEs',
    minWidth: '14rem',
  },
  {
    key: 'subcategories',
    label: $localize`:@@documentCategoriesColumnSubcategories:Subcategories actives`,
    minWidth: '9rem',
  },
];

// The subcategories of a category come all together, so the detail sorts them in the browser.
export const DOCUMENT_SUBCATEGORIES_TABLE_COLUMNS: KeyLabel[] = [
  {
    key: 'name',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`:@@documentCategoriesColumnName:Nom`,
    sortBy: 'name',
    minWidth: '14rem',
  },
  {
    key: 'nameEs',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`:@@documentCategoriesColumnNameEs:Nom en castellà`,
    sortBy: 'nameEs',
    minWidth: '14rem',
  },
];

// Route of the detail of a category, inside the Documentació maintenance.
export function documentCategoryDetailLink(id: number): (string | number)[] {
  return [
    '/',
    MAINTENANCES_ROUTES_LOC.BASE,
    MAINTENANCES_ROUTES_LOC.DOCUMENTATION,
    MAINTENANCES_ROUTES_LOC.DOCUMENT_CATEGORIES,
    id,
  ];
}

// One-navigation intent to open the detail of a category in edit mode; never kept in the URL.
export const DOCUMENT_CATEGORY_EDIT_NAVIGATION = { documentCategoryMode: 'edit' } as const;

export function isDocumentCategoryEditNavigation(info: unknown): boolean {
  return (
    typeof info === 'object' &&
    info !== null &&
    'documentCategoryMode' in info &&
    info.documentCategoryMode === 'edit'
  );
}

// The list of categories is the Documentació maintenance with its panel open.
export const DOCUMENT_CATEGORIES_LIST_LINK = [
  '/',
  MAINTENANCES_ROUTES_LOC.BASE,
  MAINTENANCES_ROUTES_LOC.DOCUMENTATION,
];
export const DOCUMENT_CATEGORIES_PANEL_FRAGMENT = 'document-categories';
