import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { map, Observable } from 'rxjs';

import { DocumentCategoryRolePermissions } from '../document-categories.model';

// Permissions of a role over the documents of a category, as the back-end sends them.
interface DocumentRolePermissionResponse {
  roleId: number;
  name: string;
  description: string | null;
  canRead: boolean;
  canUpload: boolean;
  canEdit: boolean;
  canDelete: boolean;
}

// What each role of INVAI can do with the documents of an active category, sorted by role name.
// The permissions are only set on categories, and the back-end refuses an inactive one. They are
// not cached: every save changes them.
@Injectable({ providedIn: 'root' })
export class DocumentCategoryPermissionsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'documentation/role-permission';

  getByCategory(categoryId: number): Observable<DocumentCategoryRolePermissions[]> {
    return this.http
      .get<DocumentRolePermissionResponse[]>(this.url(categoryId))
      .pipe(map((permissions) => permissions.map(toRolePermissions)));
  }

  // Sets the permissions of the given roles all at once, and answers with those of every role.
  save(
    categoryId: number,
    permissions: DocumentCategoryRolePermissions[],
  ): Observable<DocumentCategoryRolePermissions[]> {
    return this.http
      .put<DocumentRolePermissionResponse[]>(this.url(categoryId), {
        permissions: permissions.map(({ roleId, read, upload, modify, delete: remove }) => ({
          roleId,
          canRead: read,
          canUpload: upload,
          canEdit: modify,
          canDelete: remove,
        })),
      })
      .pipe(map((saved) => saved.map(toRolePermissions)));
  }
}

function toRolePermissions(response: DocumentRolePermissionResponse): DocumentCategoryRolePermissions {
  return {
    roleId: response.roleId,
    roleName: response.name,
    read: response.canRead,
    upload: response.canUpload,
    modify: response.canEdit,
    delete: response.canDelete,
  };
}
