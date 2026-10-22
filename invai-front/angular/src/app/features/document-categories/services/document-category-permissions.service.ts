import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

import { DocumentCategoryRolePermissions } from '../document-categories.model';

// The backend does not expose the permissions of the document categories yet. Until it does, they
// are kept in memory: every category starts with the default permissions of each role and keeps the
// ones saved during the session.
// TODO: Replace with the permissions endpoints of the documentation categories.
const DEFAULT_PERMISSIONS: readonly DocumentCategoryRolePermissions[] = [
  { roleId: 1, roleName: 'Administració funcional', read: true, upload: true, modify: true, delete: true },
  { roleId: 2, roleName: "Responsable d'aplicació", read: true, upload: true, modify: true, delete: false },
  { roleId: 3, roleName: 'Responsable de seguretat', read: true, upload: false, modify: false, delete: false },
  { roleId: 4, roleName: 'Consulta', read: true, upload: false, modify: false, delete: false },
];

@Injectable({ providedIn: 'root' })
export class DocumentCategoryPermissionsService {
  private readonly saved = new Map<number, DocumentCategoryRolePermissions[]>();

  getByCategory(categoryId: number): Observable<DocumentCategoryRolePermissions[]> {
    return of(copy(this.saved.get(categoryId) ?? DEFAULT_PERMISSIONS));
  }

  save(
    categoryId: number,
    permissions: DocumentCategoryRolePermissions[],
  ): Observable<DocumentCategoryRolePermissions[]> {
    this.saved.set(categoryId, copy(permissions));
    return of(copy(permissions));
  }
}

function copy(
  permissions: readonly DocumentCategoryRolePermissions[],
): DocumentCategoryRolePermissions[] {
  return permissions.map((role) => ({ ...role }));
}
