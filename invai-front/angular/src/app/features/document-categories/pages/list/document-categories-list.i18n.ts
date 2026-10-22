import { DocumentCategoryKind } from '../../components/document-category-dialog/document-category-dialog';

export const DOCUMENT_CATEGORIES_LIST_TITLE = $localize`:@@documentCategoriesTitle:Categories documentals`;
export const DOCUMENT_CATEGORIES_FILTER_NAME = $localize`:@@documentCategoriesFilterName:Nom`;
export const DOCUMENT_CATEGORIES_FILTER_STATUS = $localize`:@@documentCategoriesFilterStatus:Estat`;
export const DOCUMENT_CATEGORIES_QUICK_SEARCH_PLACEHOLDER = $localize`:@@documentCategoriesQuickSearchPlaceholder:Cerca per nom de categoria o subcategoria`;
export const DOCUMENT_CATEGORIES_QUICK_SEARCH_ARIA_LABEL = $localize`:@@documentCategoriesQuickSearchAriaLabel:Cerca ràpida de categories documentals`;
export const DOCUMENT_CATEGORIES_ADD_ARIA_LABEL = $localize`:@@documentCategoriesAddAriaLabel:Afegeix una categoria documental`;
export const DOCUMENT_CATEGORIES_ERROR_SUMMARY = $localize`:@@documentCategoriesErrorSummary:Error`;
export const DOCUMENT_CATEGORIES_LOAD_ERROR = $localize`:@@documentCategoriesLoadError:No s'han pogut carregar les categories documentals.`;
export const DOCUMENT_CATEGORY_OPTIONS_LOAD_ERROR = $localize`:@@documentCategoryOptionsLoadError:No s'han pogut carregar les categories on es pot moure la subcategoria.`;

interface KindTexts {
  loadError: string;
  createdTitle: string;
  createdDetail: string;
  updatedTitle: string;
  updatedDetail: string;
  deletedTitle: string;
  deletedDetail: string;
  restoredTitle: string;
  restoredDetail: string;
  saveError: string;
  deleteError: string;
  restoreError: string;
  deleteDialogTitle: string;
  deleteDialogMessage: (name: string) => string;
  deleteDialogCancelAriaLabel: string;
  deleteDialogConfirmAriaLabel: string;
}

export const DOCUMENT_CATEGORY_TEXTS: Record<DocumentCategoryKind, KindTexts> = {
  category: {
    loadError: $localize`:@@documentCategoryLoadError:No s'ha pogut carregar la categoria documental.`,
    createdTitle: $localize`:@@documentCategoryCreatedTitle:Categoria afegida`,
    createdDetail: $localize`:@@documentCategoryCreatedDetail:La categoria s'ha afegit correctament.`,
    updatedTitle: $localize`:@@documentCategoryUpdatedTitle:Categoria actualitzada`,
    updatedDetail: $localize`:@@documentCategoryUpdatedDetail:La categoria s'ha actualitzat correctament.`,
    deletedTitle: $localize`:@@documentCategoryDeletedTitle:Categoria eliminada`,
    deletedDetail: $localize`:@@documentCategoryDeletedDetail:La categoria s'ha eliminat correctament.`,
    restoredTitle: $localize`:@@documentCategoryRestoredTitle:Categoria restaurada`,
    restoredDetail: $localize`:@@documentCategoryRestoredDetail:La categoria s'ha restaurat correctament.`,
    saveError: $localize`:@@documentCategorySaveError:No s'ha pogut desar la categoria.`,
    deleteError: $localize`:@@documentCategoryDeleteError:No s'ha pogut eliminar la categoria.`,
    restoreError: $localize`:@@documentCategoryRestoreError:No s'ha pogut restaurar la categoria.`,
    deleteDialogTitle: $localize`:@@documentCategoryDeleteDialogTitle:Eliminar la categoria?`,
    deleteDialogMessage: (name: string) =>
      $localize`:@@documentCategoryDeleteDialogMessage:Estàs a punt d'eliminar la categoria «${name}:name:».`,
    deleteDialogCancelAriaLabel: $localize`:@@documentCategoryDeleteDialogCancel:Cancel·la l'eliminació de la categoria`,
    deleteDialogConfirmAriaLabel: $localize`:@@documentCategoryDeleteDialogConfirm:Confirma l'eliminació de la categoria`,
  },
  subcategory: {
    loadError: $localize`:@@documentSubcategoryLoadError:No s'ha pogut carregar la subcategoria documental.`,
    createdTitle: $localize`:@@documentSubcategoryCreatedTitle:Subcategoria afegida`,
    createdDetail: $localize`:@@documentSubcategoryCreatedDetail:La subcategoria s'ha afegit correctament.`,
    updatedTitle: $localize`:@@documentSubcategoryUpdatedTitle:Subcategoria actualitzada`,
    updatedDetail: $localize`:@@documentSubcategoryUpdatedDetail:La subcategoria s'ha actualitzat correctament.`,
    deletedTitle: $localize`:@@documentSubcategoryDeletedTitle:Subcategoria eliminada`,
    deletedDetail: $localize`:@@documentSubcategoryDeletedDetail:La subcategoria s'ha eliminat correctament.`,
    restoredTitle: $localize`:@@documentSubcategoryRestoredTitle:Subcategoria restaurada`,
    restoredDetail: $localize`:@@documentSubcategoryRestoredDetail:La subcategoria s'ha restaurat correctament.`,
    saveError: $localize`:@@documentSubcategorySaveError:No s'ha pogut desar la subcategoria.`,
    deleteError: $localize`:@@documentSubcategoryDeleteError:No s'ha pogut eliminar la subcategoria.`,
    restoreError: $localize`:@@documentSubcategoryRestoreError:No s'ha pogut restaurar la subcategoria.`,
    deleteDialogTitle: $localize`:@@documentSubcategoryDeleteDialogTitle:Eliminar la subcategoria?`,
    deleteDialogMessage: (name: string) =>
      $localize`:@@documentSubcategoryDeleteDialogMessage:Estàs a punt d'eliminar la subcategoria «${name}:name:».`,
    deleteDialogCancelAriaLabel: $localize`:@@documentSubcategoryDeleteDialogCancel:Cancel·la l'eliminació de la subcategoria`,
    deleteDialogConfirmAriaLabel: $localize`:@@documentSubcategoryDeleteDialogConfirm:Confirma l'eliminació de la subcategoria`,
  },
};

export const DOCUMENT_CATEGORY_DELETE_DIALOG_CANCEL_LABEL = $localize`:@@documentCategoryDeleteDialogCancelLabel:Cancel·lar`;
export const DOCUMENT_CATEGORY_DELETE_DIALOG_CONFIRM_LABEL = $localize`:@@documentCategoryDeleteDialogConfirmLabel:Eliminar`;
