import {
  ChangeDetectionStrategy,
  Component,
  input,
  model,
  output,
  signal,
} from '@angular/core';
import { PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { DialogPassThrough } from 'primeng/types/dialog';

import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import {
  CRUD_ENTITY_DIALOG_DISCARD_ARIA_LABEL,
  CRUD_ENTITY_DIALOG_DISCARD_LABEL,
  CRUD_ENTITY_DIALOG_LABELS,
  CRUD_ENTITY_DIALOG_SAVE_AND_CLOSE_ARIA_LABEL,
  CRUD_ENTITY_DIALOG_UNSAVED_MESSAGE,
  CRUD_ENTITY_DIALOG_UNSAVED_TITLE,
} from './crud-entity-dialog.i18n';

export type CrudEntityDialogMode = 'create' | 'view' | 'edit';

export interface CrudEntityDialogAriaLabels {
  accept: string;
  add: string;
  cancel: string;
  close: string;
  deactivate: string;
  edit: string;
  restore: string;
  save: string;
}

@Component({
  selector: 'app-crud-entity-dialog',
  standalone: true,
  imports: [Button, ConfirmationDialogComponent, Dialog],
  templateUrl: './crud-entity-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CrudEntityDialog {
  visible = model(false);
  title = input.required<string>();
  mode = input.required<CrudEntityDialogMode>();
  ariaLabels = input.required<CrudEntityDialogAriaLabels>();
  hasUnsavedChanges = input<() => boolean>(() => false);
  width = input('38rem');
  isLoading = input(false);
  isSaving = input(false);
  isDeleting = input(false);
  canRestore = input(false);
  canEdit = input(true);
  canDeactivate = input(true);

  edit = output<void>();
  cancelEdit = output<void>();
  submitForm = output<void>();
  deactivate = output<void>();
  restore = output<void>();
  closed = output<void>();

  protected readonly labels = CRUD_ENTITY_DIALOG_LABELS;
  protected readonly icons = PrimeIcons;
  protected readonly unsavedTitle = CRUD_ENTITY_DIALOG_UNSAVED_TITLE;
  protected readonly unsavedMessage = CRUD_ENTITY_DIALOG_UNSAVED_MESSAGE;
  protected readonly discardLabel = CRUD_ENTITY_DIALOG_DISCARD_LABEL;
  protected readonly discardAriaLabel = CRUD_ENTITY_DIALOG_DISCARD_ARIA_LABEL;
  protected readonly saveAndCloseAriaLabel =
    CRUD_ENTITY_DIALOG_SAVE_AND_CLOSE_ARIA_LABEL;
  protected readonly isUnsavedChangesDialogVisible = signal(false);
  protected readonly dialogPassThrough: DialogPassThrough = {
    mask: {
      onclick: (event: MouseEvent) => this.onMaskClick(event),
    },
    root: {
      onkeydown: (event: KeyboardEvent) => this.onDialogKeydown(event),
    },
  };

  protected isBusy(): boolean {
    return this.isLoading() || this.isSaving() || this.isDeleting();
  }

  protected requestClose(): void {
    if (this.isBusy()) return;

    if (this.mode() === 'edit' && this.hasUnsavedChanges()()) {
      this.isUnsavedChangesDialogVisible.set(true);
      return;
    }

    this.closed.emit();
  }

  protected onCancel(): void {
    if (this.isBusy()) return;

    if (this.mode() === 'edit') {
      this.cancelEdit.emit();
      return;
    }

    this.closed.emit();
  }

  protected onSubmit(): void {
    if (!this.isBusy() && this.mode() !== 'view') this.submitForm.emit();
  }

  protected onEdit(): void {
    if (
      !this.isBusy() &&
      this.canEdit() &&
      this.mode() === 'view' &&
      !this.canRestore()
    ) {
      this.edit.emit();
    }
  }

  protected onDeactivate(): void {
    if (!this.isBusy() && this.canDeactivate() && this.mode() === 'edit') {
      this.deactivate.emit();
    }
  }

  protected onRestore(): void {
    if (!this.isBusy() && this.mode() === 'view' && this.canRestore()) {
      this.restore.emit();
    }
  }

  protected discardAndClose(): void {
    this.isUnsavedChangesDialogVisible.set(false);
    this.closed.emit();
  }

  protected saveAndClose(): void {
    this.isUnsavedChangesDialogVisible.set(false);
    this.submitForm.emit();
  }

  protected closeUnsavedChangesDialog(): void {
    this.isUnsavedChangesDialogVisible.set(false);
  }

  private onMaskClick(event: MouseEvent): void {
    if (event.target !== event.currentTarget) return;
    this.requestClose();
  }

  private onDialogKeydown(event: KeyboardEvent): void {
    if (event.key !== 'Escape') return;
    event.preventDefault();
    event.stopPropagation();
    this.requestClose();
  }
}
