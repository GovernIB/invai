import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  inject,
  input,
  ViewEncapsulation,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Checkbox, CheckboxChangeEvent } from 'primeng/checkbox';
import { TableModule } from 'primeng/table';
import { switchMap } from 'rxjs';

import { DOCUMENT_PERMISSIONS } from '../../document-categories.model';
import {
  DocumentCategoryPermissionsFormArray,
  DocumentRolePermissionsFormGroup,
} from '../../forms/document-category-permissions-form.factory';
import {
  DOCUMENT_CATEGORY_PERMISSIONS_TABLE_TEXTS,
  DOCUMENT_PERMISSION_LABELS,
} from './document-category-permissions-table.i18n';

// Grid of the permissions of each role over the documents of a category. "All the permissions"
// is not stored: it is checked while every permission of the role is, and checks or unchecks them
// all. The owner disables the form when the permissions cannot be changed.
@Component({
  selector: 'app-document-category-permissions-table',
  standalone: true,
  imports: [Checkbox, FormsModule, ReactiveFormsModule, TableModule],
  templateUrl: './document-category-permissions-table.html',
  styleUrl: './document-category-permissions-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DocumentCategoryPermissionsTable {
  form = input.required<DocumentCategoryPermissionsFormArray>();

  protected readonly texts = DOCUMENT_CATEGORY_PERMISSIONS_TABLE_TEXTS;
  protected readonly labels = DOCUMENT_PERMISSION_LABELS;
  protected readonly permissions = DOCUMENT_PERMISSIONS;

  constructor() {
    // The owner replaces the rows of the same form after reading or saving the permissions;
    // reflect its events so the table shows the current rows.
    const changeDetector = inject(ChangeDetectorRef);
    toObservable(this.form)
      .pipe(switchMap((form) => form.events), takeUntilDestroyed(inject(DestroyRef)))
      .subscribe(() => changeDetector.markForCheck());
  }

  protected checkboxId(row: DocumentRolePermissionsFormGroup, column: string): string {
    return `document-category-permission-${row.controls.roleId.value}-${column}`;
  }

  protected hasAll(row: DocumentRolePermissionsFormGroup): boolean {
    return this.permissions.every((permission) => row.controls[permission].value);
  }

  protected setAll(row: DocumentRolePermissionsFormGroup, event: CheckboxChangeEvent): void {
    const granted = Boolean(event.checked);
    for (const permission of this.permissions) row.controls[permission].setValue(granted);
    row.markAsDirty();
  }
}
