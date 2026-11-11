import { DOCUMENT } from '@angular/common';
import { HelpButton } from '@core/components/help-modal/help-button';
import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  effect,
  inject,
  input,
  model,
  output,
  signal,
} from '@angular/core';
import { HELP_MODAL_LABELS } from '@core/components/help-modal/help-modal.i18n';
import { HelpModalService } from '@core/components/help-modal/help-modal.service';
import { HelpTopicPath } from '@core/components/help-modal/help-topics';
import { PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { ButtonPassThrough } from 'primeng/types/button';
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
export type CrudEntityDialogViewPrimaryAction = 'accept' | 'edit';

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
  imports: [Button, ConfirmationDialogComponent, Dialog, HelpButton],
  templateUrl: './crud-entity-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CrudEntityDialog implements OnDestroy {
  private readonly document = inject(DOCUMENT);
  private readonly helpModal = inject(HelpModalService);
  private readonly focusReturnTarget = this.captureFocusReturnTarget();

  visible = model(false);
  title = input.required<string>();
  mode = input.required<CrudEntityDialogMode>();
  ariaLabels = input.required<CrudEntityDialogAriaLabels>();
  hasUnsavedChanges = input<() => boolean>(() => false);
  width = input('38rem');
  isLoading = input(false);
  isSaving = input(false);
  submitDisabled = input(false);
  submitLabel = input<string | null>(null);
  submitIcon = input<string | null>(null);
  cancelLabel = input<string | null>(null);
  isDeleting = input(false);
  canRestore = input(false);
  canEdit = input(true);
  canDeactivate = input(true);
  showDeactivate = input(true);
  viewPrimaryAction = input<CrudEntityDialogViewPrimaryAction>('accept');
  /** Help document of the dialog: Ctrl + K and the help button open it while it is visible. */
  helpTopic = input<HelpTopicPath | null>(null);

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
  protected readonly helpLabel = HELP_MODAL_LABELS.openHelp;
  // The button does what Ctrl + K does; screen readers announce the shortcut with it.
  protected readonly helpButtonPassThrough: ButtonPassThrough = {
    root: { 'aria-keyshortcuts': 'Control+K' },
  };

  constructor() {
    effect((onCleanup) => {
      const topic = this.helpTopic();
      if (!topic || !this.visible()) return;
      onCleanup(this.helpModal.registerDialogTopic(topic));
    });
  }

  ngOnDestroy(): void {
    const target = this.focusReturnTarget;
    if (!target) return;

    queueMicrotask(() => {
      if (
        !target.isConnected ||
        target.matches(':disabled') ||
        target.getAttribute('aria-disabled') === 'true'
      ) {
        return;
      }

      target.focus();
    });
  }

  protected openHelp(): void {
    this.helpModal.open();
  }

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
    if (!this.isBusy() && !this.submitDisabled() && this.mode() !== 'view') this.submitForm.emit();
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
    if (
      !this.isBusy() &&
      this.showDeactivate() &&
      this.canDeactivate() &&
      this.mode() === 'edit'
    ) {
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
    if (this.isBusy() || this.submitDisabled()) return;
    this.isUnsavedChangesDialogVisible.set(false);
    this.submitForm.emit();
  }

  protected closeUnsavedChangesDialog(): void {
    this.isUnsavedChangesDialogVisible.set(false);
  }

  private onMaskClick(event: MouseEvent): void {
    if (event.target !== event.currentTarget) return;
    // A new entity has no warning that offers to save it: once it holds data, a click outside
    // would lose it, so only the actions of the dialog close it.
    if (this.mode() === 'create' && this.hasUnsavedChanges()()) return;
    this.requestClose();
  }

  private onDialogKeydown(event: KeyboardEvent): void {
    if (event.key !== 'Escape') return;
    // During the help opening animation, focus may still be on this dialog's button.
    // Let the help consume Escape instead of closing the underlying form.
    if (this.helpModal.visible()) return;
    event.preventDefault();
    event.stopPropagation();
    this.requestClose();
  }

  private captureFocusReturnTarget(): HTMLElement | null {
    const activeElement = this.document.activeElement;
    return activeElement instanceof HTMLElement &&
      activeElement !== this.document.body
      ? activeElement
      : null;
  }
}
