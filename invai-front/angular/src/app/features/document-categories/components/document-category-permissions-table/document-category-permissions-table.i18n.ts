import { DocumentPermission } from '../../document-categories.model';

export const DOCUMENT_CATEGORY_PERMISSIONS_TABLE_TEXTS = {
  caption: $localize`:@@documentCategoryPermissionsCaption:Permisos d'accés de cada rol`,
  role: $localize`:@@documentCategoryPermissionsRole:Rol`,
  all: $localize`:@@documentCategoryPermissionsAll:Tots els permisos`,
  granted: $localize`:@@documentCategoryPermissionsGranted:Permès`,
  denied: $localize`:@@documentCategoryPermissionsDenied:Denegat`,
  fullAccess: $localize`:@@documentCategoryPermissionsFullAccess:Accés total`,
  // Accessible name of each checkbox: the permission and the role it applies to.
  checkbox: (permission: string, role: string) =>
    $localize`:@@documentCategoryPermissionsCheckbox:${permission}:permission: per a ${role}:role:`,
};

export const DOCUMENT_PERMISSION_LABELS: Record<DocumentPermission, string> = {
  read: $localize`:@@documentPermissionRead:Lectura`,
  upload: $localize`:@@documentPermissionUpload:Càrrega`,
  modify: $localize`:@@documentPermissionModify:Modificació`,
  delete: $localize`:@@documentPermissionDelete:Eliminació (baixa lògica)`,
};
