import { FormArray, FormBuilder, FormControl, FormGroup } from '@angular/forms';

import { DocumentCategoryRolePermissions, DocumentPermission } from '../document-categories.model';

export type DocumentRolePermissionsFormControls = Record<DocumentPermission, FormControl<boolean>> & {
  roleId: FormControl<number>;
  roleName: FormControl<string>;
};

export type DocumentRolePermissionsFormGroup = FormGroup<DocumentRolePermissionsFormControls>;

// One row of permissions per role, in the order the roles come.
export type DocumentCategoryPermissionsFormArray = FormArray<DocumentRolePermissionsFormGroup>;

export function createDocumentCategoryPermissionsForm(
  formBuilder: FormBuilder,
): DocumentCategoryPermissionsFormArray {
  return formBuilder.array<DocumentRolePermissionsFormGroup>([]);
}

// Replaces the rows with the given permissions and leaves the form pristine.
export function resetDocumentCategoryPermissionsForm(
  formBuilder: FormBuilder,
  form: DocumentCategoryPermissionsFormArray,
  permissions: DocumentCategoryRolePermissions[],
): void {
  form.clear({ emitEvent: false });
  for (const role of permissions) {
    form.push(
      formBuilder.nonNullable.group({
        roleId: role.roleId,
        roleName: role.roleName,
        read: role.read,
        upload: role.upload,
        modify: role.modify,
        delete: role.delete,
      }),
      { emitEvent: false },
    );
  }
  form.markAsPristine();
  form.markAsUntouched();
  form.updateValueAndValidity();
}
