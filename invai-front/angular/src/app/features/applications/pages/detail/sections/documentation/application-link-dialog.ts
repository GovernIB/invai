import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  computed,
  DestroyRef,
  inject,
  input,
  LOCALE_ID,
  model,
  output,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { CrudEntityDialog } from '@components/crud-entity-dialog/crud-entity-dialog';
import { PrimeIcons } from 'primeng/api';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { SelectPassThrough } from 'primeng/types/select';
import { switchMap } from 'rxjs';

import { ApplicationDocumentSection } from '../../../../application-documentation.model';
import { ApplicationLinkFormGroup } from '../../../../forms/application-documentation-form.factory';
import { ApplicationDocumentationDialogMode } from './application-attachment-dialog';
import {
  APPLICATION_LINK_DESCRIPTION_MAX_LENGTH,
  APPLICATION_LINK_TITLE_MAX_LENGTH,
  APPLICATION_LINK_URL_MAX_LENGTH,
} from './application-documentation.constants';
import {
  APPLICATION_DOCUMENTATION_TEXTS,
  APPLICATION_LINK_DIALOG_TEXTS,
  APPLICATION_LINKS_TEXTS,
} from './application-documentation.i18n';
import { ApplicationDocumentationOption, documentationLabel } from './application-documentation.utils';

// Form of a link. The owner keeps the form and the persistence; this component only renders it,
// as static values while consulting.
@Component({
  selector: 'app-application-link-dialog',
  standalone: true,
  imports: [CrudEntityDialog, InputText, ReactiveFormsModule, Select],
  templateUrl: './application-link-dialog.html',
  styleUrl: './application-link-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationLinkDialog {
  visible = model(false);
  form = input.required<ApplicationLinkFormGroup>();
  mode = input.required<ApplicationDocumentationDialogMode>();
  sectionOptions = input<ApplicationDocumentationOption[]>([]);
  isSectionsLoading = input(false);
  // Section of the link being consulted, as the back-end names it.
  section = input<ApplicationDocumentSection | null>(null);
  canEdit = input(true);
  isSaving = input(false);
  isDeleting = input(false);
  submitForm = output<void>();
  closed = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();

  protected readonly texts = APPLICATION_LINK_DIALOG_TEXTS;
  protected readonly common = APPLICATION_DOCUMENTATION_TEXTS;
  protected readonly icons = PrimeIcons;
  protected readonly opensInNewTab = APPLICATION_LINKS_TEXTS.opensInNewTab;
  protected readonly titleMaxLength = APPLICATION_LINK_TITLE_MAX_LENGTH;
  protected readonly descriptionMaxLength = APPLICATION_LINK_DESCRIPTION_MAX_LENGTH;
  protected readonly urlMaxLength = APPLICATION_LINK_URL_MAX_LENGTH;
  private readonly locale = inject(LOCALE_ID);
  protected readonly sectionLabel = computed(() => documentationLabel(this.section(), this.locale));
  protected readonly title = computed(() => this.texts.titles[this.mode()]);
  protected readonly hasUnsavedChanges = () => this.mode() !== 'view' && this.form().dirty;

  constructor() {
    // Submission and cancellation come from the owner; reflect its validation/reset events.
    const changeDetector = inject(ChangeDetectorRef);
    toObservable(this.form)
      .pipe(switchMap((form) => form.events), takeUntilDestroyed(inject(DestroyRef)))
      .subscribe(() => changeDetector.markForCheck());
  }

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected errorOf(control: AbstractControl, maxLength: number): string {
    if (control.hasError('maxlength')) return this.common.maxLength(maxLength);
    if (control === this.form().controls.url && control.hasError('pattern') && control.value) {
      return this.texts.urlError;
    }
    return this.common.required;
  }

  protected sectionPassThrough(): SelectPassThrough {
    const invalid = this.isInvalid(this.form().controls.sectionId);
    return {
      label: {
        'aria-describedby': invalid ? 'application-link-section-error' : undefined,
        'aria-invalid': invalid,
        'aria-required': true,
      },
    };
  }

  protected onSubmit(): void {
    if (!this.isSaving() && !this.isDeleting() && this.mode() !== 'view') this.submitForm.emit();
  }
}
