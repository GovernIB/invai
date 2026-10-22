export const DOCUMENT_CATEGORIES_TABLE_TEXTS = {
  actionsHeader: $localize`:@@documentCategoriesActionsHeader:Accions`,
  subcategoriesOf: (name: string) =>
    $localize`:@@documentCategoriesSubcategoriesOf:Subcategories de «${name}:name:»`,
  categoryActions: (name: string) =>
    $localize`:@@documentCategoriesCategoryActions:Accions de la categoria «${name}:name:»`,
  subcategoryActions: (name: string) =>
    $localize`:@@documentCategoriesSubcategoryActions:Accions de la subcategoria «${name}:name:»`,
  view: $localize`:@@documentCategoriesActionView:Consulta`,
  edit: $localize`:@@documentCategoriesActionEdit:Edita`,
  delete: $localize`:@@documentCategoriesActionDelete:Elimina`,
  restore: $localize`:@@documentCategoriesActionRestore:Restaura`,
};
