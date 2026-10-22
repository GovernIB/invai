import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  computed,
  DestroyRef,
  ElementRef,
  inject,
  input,
  model,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { CrudEntityDialog } from '@components/crud-entity-dialog/crud-entity-dialog';
import { PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { SelectPassThrough } from 'primeng/types/select';
import { switchMap } from 'rxjs';

import {
  ApplicationDocumentCategoryOption,
  ApplicationDocumentMimeType,
} from '../../../../application-documentation.model';
import { ApplicationAttachmentFormGroup } from '../../../../forms/application-documentation-form.factory';
import {
  APPLICATION_ATTACHMENT_VERSION_MAX_LENGTH,
  APPLICATION_DOCUMENTATION_TITLE_MAX_LENGTH,
} from './application-documentation.constants';
import {
  APPLICATION_ATTACHMENT_DIALOG_TEXTS,
  APPLICATION_DOCUMENTATION_TEXTS,
} from './application-documentation.i18n';
import {
  ApplicationDocumentationOption,
  ApplicationDocumentFileRejection,
  documentFileRejectionMessage,
  documentTypeOf,
} from './application-documentation.utils';

export type ApplicationDocumentationDialogMode = 'create' | 'view' | 'edit';

// The list owns the typed form and persistence; this component renders its fields and file picker.
@Component({
  selector: 'app-application-attachment-dialog',
  standalone: true,
  imports: [
    Button,
    CrudEntityDialog,
    InputText,
    ReactiveFormsModule,
    Select,
  ],
  templateUrl: './application-attachment-dialog.html',
  styleUrl: './application-attachment-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationAttachmentDialog {
  visible = model(false);
  form = input.required<ApplicationAttachmentFormGroup>();
  mode = input.required<ApplicationDocumentationDialogMode>();
  categoryOptions = input<ApplicationDocumentCategoryOption[]>([]);
  isCategoriesLoading = input(false);
  sectionOptions = input<ApplicationDocumentationOption[]>([]);
  // Accepted file formats. Their extensions limit the file picker, and a file outside them is
  // refused before it reaches the form; without them no file can be attached.
  mimeTypes = input<ApplicationDocumentMimeType[]>([]);
  isCatalogsLoading = input(false);
  canEdit = input(true);
  isSaving = input(false);
  isDeleting = input(false);
  submitForm = output<void>();
  closed = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();

  protected readonly texts = APPLICATION_ATTACHMENT_DIALOG_TEXTS;
  protected readonly common = APPLICATION_DOCUMENTATION_TEXTS;
  protected readonly icons = PrimeIcons;
  protected readonly titleMaxLength = APPLICATION_DOCUMENTATION_TITLE_MAX_LENGTH;
  protected readonly versionMaxLength = APPLICATION_ATTACHMENT_VERSION_MAX_LENGTH;
  protected readonly accept = computed(
    () => this.mimeTypes().map(({ extension }) => '.' + extension).join(',') || null,
  );
  protected readonly title = computed(() => this.texts.titles[this.mode()]);
  protected readonly isDraggingFile = signal(false);
  protected readonly isBusy = computed(() => this.isSaving() || this.isDeleting());
  protected readonly canAttach = computed(() => this.mimeTypes().length > 0);
  protected readonly isAttachDisabled = computed(() => this.isBusy() || !this.canAttach());
  // The last refused file; the one already chosen, if any, is kept.
  protected readonly rejection = signal<ApplicationDocumentFileRejection | null>(null);
  protected readonly hasUnsavedChanges = () => this.mode() !== 'view' && this.form().dirty;
  private readonly attachButton = viewChild('attachButton', { read: ElementRef<HTMLElement> });

  constructor() {
    // The footer submits through the owner, so reflect its validation and reset events here.
    const changeDetector = inject(ChangeDetectorRef);
    toObservable(this.form)
      .pipe(switchMap((form) => form.events), takeUntilDestroyed(inject(DestroyRef)))
      .subscribe(() => changeDetector.markForCheck());
  }

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected errorOf(control: AbstractControl, maxLength: number): string {
    return control.hasError('maxlength') ? this.common.maxLength(maxLength) : this.common.required;
  }

  protected describedBy(control: AbstractControl, id: string, hint?: string): string | null {
    const ids = [hint, this.isInvalid(control) ? id + '-error' : null].filter(Boolean);
    return ids.length ? ids.join(' ') : null;
  }

  protected selectPassThrough(control: AbstractControl, id: string): SelectPassThrough {
    return {
      label: {
        'aria-describedby': this.describedBy(control, id) ?? undefined,
        'aria-invalid': this.isInvalid(control),
        'aria-required': control === this.form().controls.sectionId || control === this.form().controls.categoryId,
      },
    };
  }

  protected hasFileError(): boolean {
    return Boolean(this.rejection()) || this.isInvalid(this.form().controls.file);
  }

  protected fileDescribedBy(): string {
    const ids = ['application-attachment-file-hint', 'application-attachment-file-limits'];
    return [...ids, ...(this.hasFileError() ? ['application-attachment-file-error'] : [])].join(' ');
  }

  protected fileHint(): string {
    if (this.canAttach()) return this.texts.dropHint;
    return this.isCatalogsLoading() ? this.common.formatsLoading : this.common.formatsUnavailable;
  }

  // The refusal of the last picked file or, without one, the validation error of the file.
  protected fileAlerts(): { key: string; message: string }[] {
    const rejection = this.rejection();
    if (rejection) return [{ key: 'refused-' + rejection.attempt, message: rejection.message }];
    return this.isInvalid(this.form().controls.file) ? [{ key: 'invalid', message: this.fileError() }] : [];
  }

  protected fileError(): string {
    const control = this.form().controls.file;
    if (control.hasError('fileTooLarge')) return this.common.fileTooLarge;
    if (control.hasError('fileType')) return this.common.fileTypeNotAllowed;
    return this.common.required;
  }

  protected documentType(): string {
    const file = this.form().controls.file.value;
    return file ? documentTypeOf(file.name, this.mimeTypes()) : '—';
  }

  protected subcategoryOptions(): { id: number; label: string }[] {
    const categoryId = this.form().controls.categoryId.value;
    return this.categoryOptions().find(({ id }) => id === categoryId)?.subcategories ?? [];
  }

  protected onCategoryChange(): void {
    this.form().controls.subcategoryId.setValue(null);
  }

  protected onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.selectFile(file);
    input.value = '';
  }

  protected onFileDragOver(event: DragEvent): void {
    event.preventDefault();
    if (this.isAttachDisabled()) return;
    if (event.dataTransfer) event.dataTransfer.dropEffect = 'copy';
    this.isDraggingFile.set(true);
  }

  protected onFileDragLeave(event: DragEvent): void {
    const target = event.currentTarget as HTMLElement;
    if (!target.contains(event.relatedTarget as Node | null)) this.isDraggingFile.set(false);
  }

  protected onFileDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDraggingFile.set(false);
    const file = event.dataTransfer?.files[0];
    if (file) this.selectFile(file);
  }

  protected removeFile(): void {
    if (this.isBusy()) return;
    const control = this.form().controls.file;
    this.rejection.set(null);
    control.setValue(null);
    control.markAsDirty();
    control.markAsTouched();
    this.attachButton()?.nativeElement.querySelector('button')?.focus();
  }

  protected onSubmit(): void {
    if (!this.isBusy() && this.mode() !== 'view') this.submitForm.emit();
  }

  private selectFile(file: File): void {
    if (this.isAttachDisabled()) return;
    const message = documentFileRejectionMessage(file, this.mimeTypes());
    if (message) {
      this.rejection.set({ message, attempt: (this.rejection()?.attempt ?? 0) + 1 });
      return;
    }
    this.rejection.set(null);
    const control = this.form().controls.file;
    control.setValue(file);
    control.markAsDirty();
    control.markAsTouched();
  }
}
