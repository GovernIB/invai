import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { finalize } from 'rxjs';

import { ApplicationFormFields } from '../../components';
import { APPLICATION_STATUS_ACTIVE_ID } from '../../applications.constants';
import { ApplicationInput } from '../../applications.model';
import { createApplicationCreateForm } from '../../forms/application-form.factory';
import { ApplicationOptionsService } from '../../services/application-options.service';
import { ApplicationsService } from '../../services/applications.service';
import {
  APPLICATION_CREATE_BACK_ARIA_LABEL,
  APPLICATION_CREATE_BACK_LABEL,
  APPLICATION_CREATE_CODE_MIN_LENGTH_ERROR,
  APPLICATION_CREATE_ERROR_MESSAGE,
  APPLICATION_CREATE_ERROR_TITLE,
  APPLICATION_CREATE_LABELS,
  APPLICATION_CREATE_REQUIRED_ERROR,
  APPLICATION_CREATE_SELECT_PLACEHOLDER,
  APPLICATION_CREATE_SUBMIT_ARIA_LABEL,
  APPLICATION_CREATE_SUBMIT_LABEL,
  APPLICATION_CREATE_SUCCESS_MESSAGE,
  APPLICATION_CREATE_SUCCESS_TITLE,
} from './application-create.i18n';

@Component({
  standalone: true,
  selector: 'app-application-create-form',
  imports: [ApplicationFormFields, Button, ReactiveFormsModule],
  templateUrl: './application-create-form.html',
  styleUrl: './application-create-form.scss',
})
export class ApplicationCreateForm {
  private readonly _formBuilder = inject(FormBuilder);
  private readonly _applicationsService = inject(ApplicationsService);
  private readonly _destroyRef = inject(DestroyRef);
  private readonly _messageService = inject(MessageService);
  private readonly _optionsService = inject(ApplicationOptionsService);
  private readonly _route = inject(ActivatedRoute);
  private readonly _router = inject(Router);

  protected readonly labels = APPLICATION_CREATE_LABELS;
  protected readonly requiredError = APPLICATION_CREATE_REQUIRED_ERROR;
  protected readonly codeMinLengthError = APPLICATION_CREATE_CODE_MIN_LENGTH_ERROR;
  protected readonly selectPlaceholder = APPLICATION_CREATE_SELECT_PLACEHOLDER;
  protected readonly submitLabel = APPLICATION_CREATE_SUBMIT_LABEL;
  protected readonly submitAriaLabel = APPLICATION_CREATE_SUBMIT_ARIA_LABEL;
  protected readonly backLabel = APPLICATION_CREATE_BACK_LABEL;
  protected readonly backAriaLabel = APPLICATION_CREATE_BACK_ARIA_LABEL;
  protected readonly addIcon = PrimeIcons.PLUS;
  protected readonly backIcon = PrimeIcons.ARROW_LEFT;

  protected readonly form = createApplicationCreateForm(this._formBuilder);
  protected readonly options = toSignal(this._optionsService.getOptions(), { initialValue: null });
  protected readonly isSaving = signal(false);

  protected submit(): void {
    if (this.isSaving()) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this._applicationsService
      .create(this.toApplicationInput())
      .pipe(
        takeUntilDestroyed(this._destroyRef),
        finalize(() => this.isSaving.set(false)),
      )
      .subscribe({
        next: () => {
          this._messageService.add({
            severity: 'success',
            summary: APPLICATION_CREATE_SUCCESS_TITLE,
            detail: APPLICATION_CREATE_SUCCESS_MESSAGE,
          });
          void this._router.navigate(['..'], { relativeTo: this._route });
        },
        error: () => {
          this._messageService.add({
            severity: 'error',
            summary: APPLICATION_CREATE_ERROR_TITLE,
            detail: APPLICATION_CREATE_ERROR_MESSAGE,
          });
        },
      });
  }

  protected goBack(): void {
    void this._router.navigate(['..'], { relativeTo: this._route });
  }

  private toApplicationInput(): ApplicationInput {
    const value = this.form.getRawValue();

    return {
      name: value.application,
      prefix: value.prefix,
      code: value.code,
      categoryId: value.category as number,
      systemTypeId: value.informationSystem as number,
      fieldId: value.scope as number,
      admUnitId: value.administrativeUnit as number,
      commissionId: value.commission as number,
      description: value.description || null,
      statusId: APPLICATION_STATUS_ACTIVE_ID,
    };
  }
}
