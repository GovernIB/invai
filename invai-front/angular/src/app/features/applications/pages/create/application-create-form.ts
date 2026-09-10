import { Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { normalizeQuillHtml } from '@shared/utils/rich-text.utils';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Subject, catchError, finalize, map, of, switchMap, tap } from 'rxjs';

import { ApplicationFormFields } from '../../components';
import { APPLICATION_STATUS_ACTIVE_ID } from '../../applications.constants';
import { ApplicationInput } from '../../applications.model';
import { createApplicationCreateForm } from '../../forms/application-form.factory';
import {
  ApplicationCommissionOption,
  ApplicationOptionsService,
  ApplicationSelectOptions,
} from '../../services/application-options.service';
import { ApplicationsService } from '../../services/applications.service';
import {
  APPLICATION_CREATE_BACK_ARIA_LABEL,
  APPLICATION_CREATE_BACK_LABEL,
  APPLICATION_CREATE_CODE_MAX_LENGTH_ERROR,
  APPLICATION_CREATE_CODE_MIN_LENGTH_ERROR,
  APPLICATION_CREATE_ERROR_MESSAGE,
  APPLICATION_CREATE_ERROR_TITLE,
  APPLICATION_CREATE_LABELS,
  APPLICATION_CREATE_PREFIX_MAX_LENGTH_ERROR,
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
  private readonly _applicationOptionsService = inject(ApplicationOptionsService);
  private readonly _destroyRef = inject(DestroyRef);
  private readonly _messageService = inject(MessageService);
  private readonly _route = inject(ActivatedRoute);
  private readonly _router = inject(Router);

  protected readonly labels = APPLICATION_CREATE_LABELS;
  protected readonly requiredError = APPLICATION_CREATE_REQUIRED_ERROR;
  protected readonly prefixMaxLengthError = APPLICATION_CREATE_PREFIX_MAX_LENGTH_ERROR;
  protected readonly codeMinLengthError = APPLICATION_CREATE_CODE_MIN_LENGTH_ERROR;
  protected readonly codeMaxLengthError = APPLICATION_CREATE_CODE_MAX_LENGTH_ERROR;
  protected readonly selectPlaceholder = APPLICATION_CREATE_SELECT_PLACEHOLDER;
  protected readonly submitLabel = APPLICATION_CREATE_SUBMIT_LABEL;
  protected readonly submitAriaLabel = APPLICATION_CREATE_SUBMIT_ARIA_LABEL;
  protected readonly backLabel = APPLICATION_CREATE_BACK_LABEL;
  protected readonly backAriaLabel = APPLICATION_CREATE_BACK_ARIA_LABEL;
  protected readonly addIcon = PrimeIcons.PLUS;
  protected readonly backIcon = PrimeIcons.ARROW_LEFT;

  protected readonly form = createApplicationCreateForm(this._formBuilder);
  readonly options = input.required<ApplicationSelectOptions>();
  readonly departmentsLoadFailed = input(false);
  protected readonly isSaving = signal(false);
  protected readonly isDepartmentsLoading = signal(false);
  protected readonly isAdministrativeUnitsLoading = signal(false);
  private readonly departmentsOverride = signal<ApplicationSelectOptions['departments'] | null>(
    null,
  );
  private readonly administrativeUnits = signal<ApplicationSelectOptions['administrativeUnits']>(
    [],
  );
  private readonly departmentsFailedOverride = signal<boolean | null>(null);
  protected readonly hasDepartmentsLoadFailed = computed(
    () => this.departmentsFailedOverride() ?? this.departmentsLoadFailed(),
  );
  protected readonly hasAdministrativeUnitsLoadFailed = signal(false);
  protected readonly currentOptions = computed<ApplicationSelectOptions>(() => ({
    ...this.options(),
    departments: this.departmentsOverride() ?? this.options().departments,
    administrativeUnits: this.administrativeUnits(),
  }));
  private readonly administrativeUnitRequests = new Subject<string | null>();

  constructor() {
    this.administrativeUnitRequests
      .pipe(
        tap((code) => {
          this.isAdministrativeUnitsLoading.set(Boolean(code));
          this.hasAdministrativeUnitsLoadFailed.set(false);
          this.administrativeUnits.set([]);
          this.form.controls.administrativeUnit.disable({ emitEvent: false });
        }),
        switchMap((code) =>
          code
            ? this._applicationOptionsService.getAdministrativeUnitOptions(code).pipe(
                map((options) => ({ options, failed: false })),
                catchError(() => of({ options: [], failed: true })),
              )
            : of({ options: [], failed: false }),
        ),
        takeUntilDestroyed(this._destroyRef),
      )
      .subscribe(({ options, failed }) => {
        this.isAdministrativeUnitsLoading.set(false);
        this.hasAdministrativeUnitsLoadFailed.set(failed);
        this.administrativeUnits.set(options);
        if (!failed && this.form.controls.conselleria.value) {
          this.form.controls.administrativeUnit.enable({ emitEvent: false });
        }
      });
  }

  protected submit(): void {
    if (this.isSaving()) return;

    if (this.form.invalid || this.form.controls.administrativeUnit.disabled) {
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
        error: (error: unknown) => {
          if (isStructuredBadRequest(error)) return;

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

  protected selectCommission(commission: ApplicationCommissionOption | null): void {
    this.form.patchValue({
      commissionExpedientNumber: commission?.expedientNumber ?? '',
      commissionApprovalDate: commission?.approvalDate ?? '',
      commissionType: commission?.commissionType ?? null,
    });
  }

  protected selectConselleria(code: string | null): void {
    this.form.controls.administrativeUnit.reset(null, { emitEvent: false });
    this.administrativeUnitRequests.next(code);
  }

  protected retryAdministrativeUnits(): void {
    this.administrativeUnitRequests.next(this.form.controls.conselleria.value);
  }

  protected retryDepartments(): void {
    if (this.isDepartmentsLoading()) return;

    this.isDepartmentsLoading.set(true);
    this._applicationOptionsService
      .getDepartmentOptions()
      .pipe(
        takeUntilDestroyed(this._destroyRef),
        finalize(() => this.isDepartmentsLoading.set(false)),
      )
      .subscribe({
        next: (departments) => {
          this.departmentsOverride.set(departments);
          this.departmentsFailedOverride.set(false);
        },
        error: () => this.departmentsFailedOverride.set(true),
      });
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
      admUnitCode: value.administrativeUnit as string,
      commissionId: value.commission as number,
      description: normalizeQuillHtml(value.description),
      statusId: APPLICATION_STATUS_ACTIVE_ID,
    };
  }
}
