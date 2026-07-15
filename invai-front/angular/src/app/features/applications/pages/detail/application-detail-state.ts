import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder } from '@angular/forms';
import { Observable, map, of, tap } from 'rxjs';

import { APPLICATION_STATUS_ACTIVE_ID } from '../../applications.constants';
import {
  ApplicationDetailFormValue,
  createApplicationDetailForm,
  createApplicationSystemsDatabasesForm,
} from '../../forms/application-form.factory';
import { Application, ApplicationInput, ApplicationOutput } from '../../applications.model';
import { ApplicationsService } from '../../services/applications.service';

export type ApplicationGeneralFormValue = ApplicationDetailFormValue;

const EMPTY_FORM_VALUE: ApplicationDetailFormValue = {
  application: '',
  category: null,
  informationSystem: null,
  scope: null,
  administrativeUnit: null,
  creationDate: '',
  modificationDate: '',
  withdrawalDate: '',
  commission: null,
  prefix: '',
  description: '',
};

@Injectable()
export class ApplicationDetailState {
  private readonly applicationsService = inject(ApplicationsService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private savedValue: ApplicationDetailFormValue = EMPTY_FORM_VALUE;

  readonly application = signal<Application | null>(null);
  readonly isEditing = signal(false);

  readonly form = createApplicationDetailForm(this.formBuilder);
  readonly systemsDatabasesForm = createApplicationSystemsDatabasesForm(this.formBuilder);

  constructor() {
    this.form.disable({ emitEvent: false });
  }

  load(applicationId: string | null): void {
    const id = Number(applicationId);
    if (!applicationId || Number.isNaN(id)) {
      this.resetState();
      return;
    }

    this.applicationsService
      .getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.setApplication(response);
        },
        error: () => {
          this.resetState();
        },
      });
  }

  startEditing(): void {
    if (!this.application()) return;

    this.form.enable({ emitEvent: false });
    this.disableSystemDates();
    this.isEditing.set(true);
  }

  cancelEditing(): void {
    this.form.reset(this.savedValue, { emitEvent: false });
    this.form.disable({ emitEvent: false });
    this.isEditing.set(false);
  }

  save(): Observable<boolean> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return of(false);
    }

    const current = this.application();
    const id = Number(current?.id);
    if (!current || Number.isNaN(id)) return of(false);

    return this.applicationsService.update(id, this.toApplicationInput(current)).pipe(
      tap((response) => this.setApplication(response)),
      map(() => true),
    );
  }

  private resetState(): void {
    this.application.set(null);
    this.savedValue = { ...EMPTY_FORM_VALUE };
    this.form.reset(this.savedValue, { emitEvent: false });
    this.systemsDatabasesForm.reset({ observations: '' }, { emitEvent: false });
    this.form.disable({ emitEvent: false });
    this.isEditing.set(false);
  }

  private setApplication(response: ApplicationOutput): void {
    const application = this.applicationsService.toApplication(response);
    this.application.set(application);
    this.savedValue = this.toFormValue(application);
    this.form.reset(this.savedValue, { emitEvent: false });
    this.systemsDatabasesForm.reset({ observations: '' }, { emitEvent: false });
    this.form.disable({ emitEvent: false });
    this.isEditing.set(false);
  }

  private disableSystemDates(): void {
    this.form.controls.creationDate.disable({ emitEvent: false });
    this.form.controls.modificationDate.disable({ emitEvent: false });
    this.form.controls.withdrawalDate.disable({ emitEvent: false });
  }

  private toFormValue(application: Application): ApplicationDetailFormValue {
    return {
      application: application.name,
      category: application.categoryId ?? null,
      informationSystem: application.informationSystemId ?? null,
      scope: application.scopeId ?? null,
      administrativeUnit: application.administrativeUnitId ?? null,
      creationDate: application.creationDate,
      modificationDate: application.modificationDate,
      withdrawalDate: application.withdrawalDate,
      commission: application.commissionId ?? null,
      prefix: application.prefix,
      description: application.description,
    };
  }

  private toApplicationInput(current: Application): ApplicationInput {
    const value = this.form.getRawValue();

    return {
      name: value.application,
      prefix: value.prefix,
      code: current.code,
      categoryId: value.category as number,
      systemTypeId: value.informationSystem as number,
      fieldId: value.scope as number,
      admUnitId: value.administrativeUnit as number,
      commissionId: value.commission as number,
      description: value.description || null,
      statusId: current.statusId ?? APPLICATION_STATUS_ACTIVE_ID,
    };
  }
}
